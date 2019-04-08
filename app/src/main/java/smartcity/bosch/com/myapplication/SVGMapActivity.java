package smartcity.bosch.com.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.bosch.smartcity.lib.shortestway.DijkstraShortestPath;
import com.bosch.smartcity.lib.shortestway.Graph;
import com.bosch.smartcity.lib.shortestway.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SVGMapActivity extends AppCompatActivity {

    WebView wvMap;

    String routeData;

    Graph graph;

    List<SVGBlockInfo> blockInfos = new ArrayList<>();
    List<RouteInfo> routeInfos = new ArrayList<>();
    List<Node> nodeList = new ArrayList<>();
    List<SVGBlockInfo> selectedBlocks = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svgmap);
        wvMap = findViewById(R.id.wvMap);
        wvMap.getSettings().setJavaScriptEnabled(true);
        wvMap.getSettings().setLoadWithOverviewMode(true);
        wvMap.getSettings().setUseWideViewPort(true);wvMap.setPadding(0, 0, 0, 0);
        wvMap.addJavascriptInterface(new WebAppInterface(), "Android");

        String svg = loadSvg();
        wvMap.loadData(svg, "image/svg+xml", "utf-8");
        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllRoute();
                try {
                    hideAllRoute();
                    buildGraph();
                    if(selectedBlocks.size() == 2) {
                        Node node1 = getNodeFromList(nodeList, selectedBlocks.get(0).getSimpleName().toLowerCase());
                        Node node2 = getNodeFromList(nodeList, selectedBlocks.get(1).getSimpleName().toLowerCase());
                        List<Node> shortestPath = findShortestParth(graph, node1,
                                node2);
                        drawShortestPath(shortestPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    String loadSvg() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    getAssets().open("floormap_route.svg")));
            StringBuffer buf = new StringBuffer();
            String s = null;
            while ((s = input.readLine()) != null) {
                buf.append(s);
                buf.append('\n');
            }
            input.close();
            return buf.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void processBlockData(String blockData) {
        try {
            JSONArray jsonArray = new JSONArray(blockData);
            for (int i = 0; i < jsonArray.length(); i++) {
                SVGBlockInfo blockInfo = getBlockInfo(jsonArray.get(i).toString());
                if(blockInfo != null) {
                    blockInfos.add(blockInfo);
                    Log.i("LOG", "Added block:" + blockInfo.getId());
                }
            }
            Log.i("LOG", "Total block object:" + blockInfos.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SVGBlockInfo getBlockInfo(String blockString) {
        if(TextUtils.isEmpty(blockString))
            return null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(blockString));
            pullParser.nextTag();
            int eventType = pullParser.getEventType();

            if(eventType == XmlPullParser.START_TAG) {
                SVGBlockInfo blockInfo = new SVGBlockInfo();
                blockInfo.setId(pullParser.getAttributeValue(null, "id"));
                blockInfo.setFillColor(pullParser.getAttributeValue(null, "fill"));
                blockInfo.setStrokeColor(pullParser.getAttributeValue(null, "stroke"));
                blockInfo.setStrokeMiterLimit(Integer.parseInt(pullParser.getAttributeValue(null, "stroke-miterlimit")));
                return blockInfo;
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private void processRouteData(String routeData) {
        if(TextUtils.isEmpty(routeData))
            return;
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(routeData));
            int eventType = pullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG && pullParser.getName().equals("path")) {
                    RouteInfo routeInfo = new RouteInfo();
                    routeInfo.setId(pullParser.getAttributeValue(null, "id"));
                    routeInfo.setFill(pullParser.getAttributeValue(null, "fill"));
                    routeInfo.setStroke(pullParser.getAttributeValue(null, "stroke"));
//                    routeInfo.setStrokeOpacity(Float.parseFloat(pullParser.getAttributeValue(null, "stroke-opacity")));
                    routeInfos.add(routeInfo);
                }
                eventType = pullParser.next();
            }
            Log.i("LOG", "total route:" + routeInfos.size());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void buildGraph() throws Exception {
        Set<String> nodes = new HashSet<String>();
        for(RouteInfo routeInfo: routeInfos) {
            String[] nodesOfE = routeInfo.getId().split("_");
            if(nodesOfE == null || nodesOfE.length != 2)
                throw new Exception("Invalid format of edge:" + routeInfo.getId());
            nodes.add(nodesOfE[0]);
            nodes.add(nodesOfE[1]);
        }

        //Create node
        nodeList = new ArrayList<>();
        for (String nodeName : nodes) {
            Node node = new Node(nodeName);
            nodeList.add(node);
        }

        for(RouteInfo routeInfo : routeInfos) {

            //debug
            if(routeInfo.getId().contains("powell")) {
                Log.i("TAG", "");
            }

            String[] nodesOfE = routeInfo.getId().split("_");

            Node startNode = getNodeFromList(nodeList, nodesOfE[0]);
            Node tailNode = getNodeFromList(nodeList, nodesOfE[1]);
            startNode.addDestination(tailNode, 1);
            tailNode.addDestination(startNode, 1);
        }

        Graph graph = new Graph();
        for(Node node : nodeList) {
            graph.addNode(node);
        }
    }

    private List<Node> findShortestParth(Graph graph, Node startNode, Node destNode) {
        if(startNode == null || destNode == null) {
            return null;
        }

        Graph tempGraph = DijkstraShortestPath.calculateShortestPathFromSource(graph, startNode);

        //add the destination node to list
        List<Node> completePath = destNode.getShortestPath();
        completePath.add(destNode);

        return completePath;
    }

    private void drawShortestPath(List<Node> nodes) {
        //Get the route of path
        List<RouteInfo> routeOfPath = new ArrayList<>();
        for(int i = 0; i < nodes.size() - 1; i++) {
            Node startNode = nodes.get(i);
            Node endNode = nodes.get(i + 1);
            RouteInfo routeInfo = getRouteForNodes(startNode, endNode);
            if(routeOfPath != null) {
                routeOfPath.add(routeInfo);
            }
        }

        //draw route
        for(RouteInfo routeInfo : routeOfPath) {
            hideRoute(routeInfo, true);
        }
    }

    private RouteInfo getRouteForNodes(Node startNode, Node endNode) {
        for(RouteInfo routeInfo : routeInfos) {
            String[] parts = routeInfo.getId().split("_");
            if((parts[0].equals(startNode.getName()) && parts[1].equals(endNode.getName()))
                    || (parts[0].equals(endNode.getName()) && parts[1].equals(startNode.getName()))) {
                return routeInfo;
            }
        }
        return null;
    }

    private Node getNodeFromList(List<Node> nodes, String name) {
        for(Node node : nodes) {
            if(node.getName().equals(name))
                return node;
        }
        return null;
    }

    private void hideRoute(RouteInfo routeInfo, boolean isShowed) {
        changeAttribute(wvMap, routeInfo.getId(), "display", (isShowed ? "yes" : "none"));
    }

    private void hideAllRoute() {
        if(routeInfos != null) {
            for(RouteInfo route : routeInfos) {
                hideRoute(route, false);
            }
        }
    }

    private void highlightBlock(String blockId, String color) {
        changeAttribute(wvMap, blockId, "fill", color);
    }

    private SVGBlockInfo getBlockById(String blockId) {
        for(SVGBlockInfo blockInfo : blockInfos) {
            if(blockId.equalsIgnoreCase(blockInfo.getId()))
                return blockInfo;
        }
        return null;
    }

    private void changeAttribute(final WebView webView, final String elementId, final String key, final String value) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:changeAttribute('" + elementId + "','" + key +  "','"  + value +  "')");
            }
        });

    }

    public class WebAppInterface {
        /** Show a toast from svg */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(SVGMapActivity.this, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void log(String log) {
            Log.i("Webview", log);
        }

        @JavascriptInterface
        public void routeData(String route) {
            routeData = route.toString();
            processRouteData(routeData);
        }

        @JavascriptInterface
        public void blockData(String blocks) {
            Log.i("Webview", blocks);
            processBlockData(blocks);
        }

        @JavascriptInterface
        public void selectBlock(String blockId) {
            if(selectedBlocks.size() == 2) {
                SVGBlockInfo newSelectedBlock = getBlockById(blockId);
                if(newSelectedBlock != null) {
                    SVGBlockInfo blockInfo = selectedBlocks.get(0); //first block should be remove
                    highlightBlock(blockInfo.getId(), "#FFFFFF"); //clear highlight
                    highlightBlock(newSelectedBlock.getId(), "#FFFF00");
                    selectedBlocks.remove(blockInfo);
                    selectedBlocks.add(newSelectedBlock);
                }
            } else {
                final SVGBlockInfo newSelectedBlock = getBlockById(blockId);
                if(newSelectedBlock != null) {
                    selectedBlocks.add(newSelectedBlock);
                    highlightBlock(newSelectedBlock.getId(), "#FFFF00");

                }
            }
        }
    }
}


