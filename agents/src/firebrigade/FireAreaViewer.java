package firebrigade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import rescuecore2.Timestep;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.view.AnimatedWorldModelViewer;
import rescuecore2.view.LayerViewComponent;
public class FireAreaViewer extends StandardViewer  {
    private static final int DEFAULT_FONT_SIZE = 20;
    private static final int PRECISION = 3;

    private static final String FONT_SIZE_KEY = "viewer.font-size";
    private static final String MAXIMISE_KEY = "viewer.maximise";
    private static final String TEAM_NAME_KEY = "viewer.team-name";
    
    private LayerViewComponent viewer;
    
    @Override
    protected void postConnect() {
        super.postConnect();

        JFrame frame = new JFrame("Viewer " + getViewerID() + " (" + model.getAllEntities().size() + " entities)");
        viewer = new AnimatedWorldModelViewer();
        viewer.initialise(config);
        viewer.view(model);
        final FireAreaViewLayer faLayer = new FireAreaViewLayer();
        viewer.addLayer(faLayer);
        // CHECKSTYLE:OFF:MagicNumber
        viewer.setPreferredSize(new Dimension(500, 500));
        // CHECKSTYLE:ON:MagicNumber
        
        frame.add(viewer, BorderLayout.CENTER);
        frame.pack();
        if (config.getBooleanValue(MAXIMISE_KEY, false)) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        frame.setVisible(true);
//        viewer.addViewListener(new ViewListener() {
//                @Override
//                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
//                    for (RenderedObject next : objects) {
//                        System.out.println(next.getObject());
//                        graphView.SetClickedObject(next.getObject());
//                        
//                        viewer.repaint();
//                    }
//                }
//                
//
//                @Override
//                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
//                }
//            });
    }

    @Override
    protected void handleTimestep(final KVTimestep t) {
        super.handleTimestep(t);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    viewer.view(model, t.getCommands());
                    viewer.repaint();
                }
            });
    }

    @Override
    public String toString() {
        return "Fire area viewer";
    }
}
