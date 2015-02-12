/*
 *  File: TimelineExample.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package cn.zju.edu.timeline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.date.Interval;
import de.jaret.util.date.JaretDate;
import de.jaret.util.ui.timebars.TimeBarViewerDelegate;
import de.jaret.util.ui.timebars.TimeBarViewerInterface;
import de.jaret.util.ui.timebars.model.DefaultTimeBarNode;
import de.jaret.util.ui.timebars.model.FocussedIntervalListener;
import de.jaret.util.ui.timebars.model.IRowHeightStrategy;
import de.jaret.util.ui.timebars.model.ISelectionRectListener;
import de.jaret.util.ui.timebars.model.ITimeBarViewState;
import de.jaret.util.ui.timebars.model.PPSInterval;
import de.jaret.util.ui.timebars.model.TimeBarModel;
import de.jaret.util.ui.timebars.model.TimeBarRow;
import de.jaret.util.ui.timebars.swt.TimeBarViewer;
import de.jaret.util.ui.timebars.swt.renderer.BoxTimeScaleRenderer;
import de.jaret.util.ui.timebars.swt.renderer.DefaultGridRenderer;
import de.jaret.util.ui.timebars.swt.util.TimeScaleDragSupport;
import de.jaret.util.ui.timebars.swt.util.actions.JaretTimeBarsActionFactory;
import cn.zju.edu.model.EventTimeBarRow;
import cn.zju.edu.timeline.model.*;
import cn.zju.edu.timeline.renderer.*;
import cn.zju.edu.*;
import cn.zju.edu.manager.*;

/**
 * Example showing a combination of two timebarviewers on a single model in a timeline fashion. The idea has been taken
 * from http://simile.mit.edu/timeline/.
 * 
 * @author Peter Kliem
 * @version $Id: SwtOverlapExample.java 559 2007-09-10 23:13:47Z olk $
 */
public class TimelineExample extends ApplicationWindow {
    /** upper timebar viewer. */
    private static TimeBarViewer _tbv;
    /** lower timebar viewer. */
    private static TimeBarViewer _tbv2;

    /** factor the lower viewer is zoomed out. */
    private double LOWERFACTOR = 12.0;
    /** factor the hotspots are zoomed in. */
    private double ZOOMFACTOR = 20.0;

    /** number of rows to display. */
    private int NUMROWS = 20;

    /** helper to prevent endless loop in prop changes between the viewers. */
    boolean ignorePropChangeFlag = false;

    public TimelineExample() {
        super(null);
    }

    protected Control createContents(Composite parent)
    {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.verticalSpacing = 0;
        parent.setLayout(gridLayout);

        // create (load) the model
        ModelCreator creator = new ModelCreator("/cn/zju/edu/timeline/model/jfk.xml");
        //TimeBarModel model = creator.getModel();
        
        TimeBarModel model;
        try
        {
        	HistoryActivityManager.getInstance().retrieveHistroy();
        	model = HistoryActivityManager.getInstance().createTimeModel();
	        /*
        	DataManager dm = new DataManager();
	        String sql = "select * from tbl_interactions where user_name = '" + "baolingfeng" + "'" 
					//+ " and " + "timestamp>='2015-02-04 15:25:10.937' and timestamp<='2015-02-04 15:25:32.568'"
					+ " order by timestamp";
	        
	        dm.getInteractionData(sql);
	        dm.aggrLLInteractions();
	        		
	        model = dm.createTimeModel();
	        */
	        System.out.println("Model start "+model.getMinDate().toDisplayString());
	        System.out.println("Model end "+model.getMaxDate().toDisplayString());
        }
        catch(Exception e)
        {
        	model = creator.getModel();
        	e.printStackTrace();
        }
        
        GridData gd = new GridData(GridData.FILL_BOTH);

        // create the upper time bar viwer
        // no scroll bars for this one
        _tbv = new TimeBarViewer(parent, SWT.NULL);
        _tbv.setLayoutData(gd);
        _tbv.setName("upper");

        _tbv.setTimeScalePosition(TimeBarViewer.TIMESCALE_POSITION_TOP);
        _tbv.setModel(model);

        _tbv.setPixelPerSecond(0.0014351851851851852);

        configureTBV(_tbv);
        // do not mark weekends
        ((DefaultGridRenderer) _tbv.getGridRenderer()).setMarkWeekends(false);

        // register the renderer for the event detail
        _tbv.registerTimeBarRenderer(TimelineEvent.class, new DetailEventRenderer());
        
        // set the look forward/back properties to include a wider range
        _tbv.setScrollLookBackMinutes(24 * 60 * 3);
        _tbv.setScrollLookForwardMinutes(24 * 60 * 3);
        _tbv.addFocussedIntervalListener(new FocussedIntervalListener() {
            public void focussedIntervalChanged(Object source, TimeBarRow row, Interval interval) {
            	System.out.println("focus listener...");
            	if (interval != null) {
                    System.out.println(interval.toString());
                } 
            }
            
        });
       
        MenuManager mm = new MenuManager();
        mm.add(new DummyAction(_tbv));
        _tbv.setIntervalContextMenu(mm.createContextMenu(_tbv));
        
        _tbv.addMouseListener(new MouseListener()
        {
        	public void mouseDown(MouseEvent e) {
        		
        		if(_tbv.getFocussedInterval() != null)
        		{
        			System.out.println("Mouse: " + _tbv.getFocussedInterval().toString() + "(" + _tbv.getFocussedInterval().getBegin() + ")");
        		}
			  }
			
			  public void mouseUp(MouseEvent e) {
			   
			  }
			
			  public void mouseDoubleClick(MouseEvent e) {
			
			  }
        });
       
        // the second (lower) viewer
        // only horizontal scroll
        _tbv2 = new TimeBarViewer(parent, SWT.H_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        _tbv2.setLayoutData(gd);
        _tbv2.setName("lower");

        _tbv2.setTimeScalePosition(TimeBarViewer.TIMESCALE_POSITION_BOTTOM);
        _tbv2.setModel(model);

        _tbv2.setPixelPerSecond(_tbv.getPixelPerSecond() / LOWERFACTOR);

        final LowerGridRenderer gridRenderer = new LowerGridRenderer();
        _tbv2.setGridRenderer(gridRenderer);

        // register the renderer for the event detail
        _tbv2.registerTimeBarRenderer(TimelineEvent.class, new OverviewEventRenderer());

        configureTBV(_tbv2);

        // create pps intervals for dynamic scaling
        // this defines the "hot spots"
        _tbv.setVariableXScale(true);
        DefaultTimeBarNode scaleRow = (DefaultTimeBarNode) _tbv.getPpsRow();
        PPSInterval i = new PPSInterval(_tbv.getPixelPerSecond() * ZOOMFACTOR);
        i.setBegin(new JaretDate(02, 02, 2015, 0, 0, 0));
        i.setEnd(new JaretDate(02, 02, 2015, 18, 0, 0));
        scaleRow.addInterval(i);
        i = new PPSInterval(_tbv.getPixelPerSecond() * ZOOMFACTOR * 3);
        i.setBegin(new JaretDate(02, 02, 2015, 18, 0, 0));
        i.setEnd(new JaretDate(03, 02, 2015, 0, 0, 0));
        scaleRow.addInterval(i);
        i = new PPSInterval(_tbv.getPixelPerSecond() * ZOOMFACTOR);
        i.setBegin(new JaretDate(03, 02, 2015, 0, 0, 0));
        i.setEnd(new JaretDate(04, 02, 2015, 0, 0, 0));
        scaleRow.addInterval(i);

        // create pps intervals lower
        _tbv2.setVariableXScale(true);
        DefaultTimeBarNode scaleRowLower = (DefaultTimeBarNode) _tbv2.getPpsRow();
        if (_tbv.getPpsRow() != null) {
            for (Interval interval : _tbv.getPpsRow().getIntervals()) {
                PPSInterval ppsInterval = (PPSInterval) interval;
                i = new PPSInterval(ppsInterval.getPps() / LOWERFACTOR);
                i.setBegin(ppsInterval.getBegin().copy());
                i.setEnd(ppsInterval.getEnd().copy());
                scaleRowLower.addInterval(i);
            }
        }

        // property change listeners on both viewers, modifying the other viewer and the scale od the pps intervals
        // ensure that the viewers do work smoothly
        // Since there are some values set on the other viewer that have been calculated, a flag ensures, that we do not
        // end up in an endless loop

        // adapt scale intervals and lower viewer whenever the scale of the upper viewer changes
        _tbv.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (ignorePropChangeFlag) {
                    ignorePropChangeFlag = false;
                    return;
                }
                if (evt.getPropertyName().equals(TimeBarViewerInterface.PROPERTYNAME_PIXELPERSECOND)) {
                    ignorePropChangeFlag = true;
                    _tbv2.setPixelPerSecond(_tbv.getPixelPerSecond() / LOWERFACTOR);

                    if (_tbv.getPpsRow() != null) {
                        for (Interval interval : _tbv.getPpsRow().getIntervals()) {
                            PPSInterval ppsInterval = (PPSInterval) interval;
                            ppsInterval.setPps(_tbv.getPixelPerSecond() * ZOOMFACTOR);
                        }
                    }
                    if (_tbv2.getPpsRow() != null) {
                        for (Interval interval : _tbv2.getPpsRow().getIntervals()) {
                            PPSInterval ppsInterval = (PPSInterval) interval;
                            ppsInterval.setPps(_tbv2.getPixelPerSecond() * ZOOMFACTOR);
                        }
                    }
                } else if (evt.getPropertyName().equals(TimeBarViewerInterface.PROPERTYNAME_STARTDATE)) {
                    JaretDate midDate = getMidDate(_tbv);
                    ignorePropChangeFlag = true;
                    setMidDate(_tbv2, midDate);

                    gridRenderer.setStartMark(_tbv.getStartDate().copy());
                    gridRenderer.setEndMark(_tbv.getStartDate().copy().advanceSeconds(_tbv.getSecondsDisplayed()));
                    _tbv2.redraw();

                }
            }

        });

        // adapt scale intervals and upper viewer whenever the scale of the lower viewer changes
        _tbv2.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (ignorePropChangeFlag) {
                    ignorePropChangeFlag = false;
                    return;
                }
                if (evt.getPropertyName().equals(TimeBarViewerInterface.PROPERTYNAME_PIXELPERSECOND)) {
                    ignorePropChangeFlag = true;
                    _tbv.setPixelPerSecond(_tbv2.getPixelPerSecond() / LOWERFACTOR);

                    if (_tbv.getPpsRow() != null) {
                        for (Interval interval : _tbv.getPpsRow().getIntervals()) {
                            PPSInterval ppsInterval = (PPSInterval) interval;
                            ppsInterval.setPps(_tbv.getPixelPerSecond() * ZOOMFACTOR);
                        }
                    }
                    if (_tbv2.getPpsRow() != null) {
                        for (Interval interval : _tbv2.getPpsRow().getIntervals()) {
                            PPSInterval ppsInterval = (PPSInterval) interval;
                            ppsInterval.setPps(_tbv2.getPixelPerSecond() * ZOOMFACTOR);
                        }
                    }
                } else if (evt.getPropertyName().equals(TimeBarViewerInterface.PROPERTYNAME_STARTDATE)) {
                    JaretDate midDate = getMidDate(_tbv2);
                    ignorePropChangeFlag = true;
                    setMidDate(_tbv, midDate);

                    gridRenderer.setStartMark(_tbv.getStartDate().copy());
                    gridRenderer.setEndMark(_tbv.getStartDate().copy().advanceSeconds(_tbv.getSecondsDisplayed()));
                    _tbv2.redraw();
                }
            }

        });

        _tbv.setStartDate(new JaretDate(20, 11, 1963, 0, 0, 0));
        _tbv2.redraw();

        return _tbv;
    }

    /**
     * Helper retrieving the date displayed in the middle of a viewer.
     * 
     * @param tbv the viewer
     * @return the date in the middle of the viewer
     */
    private JaretDate getMidDate(TimeBarViewer tbv) {
        TimeBarViewerDelegate delegate = (TimeBarViewerDelegate) tbv.getData("delegate");
        Rectangle diagramRect = TimeBarViewer.convertRect(delegate.getDiagramRect());
        JaretDate midDate = tbv.dateForX(diagramRect.x + diagramRect.width / 2);
        return midDate;
    }

    /**
     * Helper setting the date displayed in the middle of a timebar viewer.
     * 
     * @param tbv the viewer
     * @param midDate the date
     */
    private void setMidDate(TimeBarViewer tbv, JaretDate midDate) {
        TimeBarViewerDelegate delegate = (TimeBarViewerDelegate) tbv.getData("delegate");
        Rectangle diagramRect = TimeBarViewer.convertRect(delegate.getDiagramRect());

        int absX = delegate.xForDateAbs(midDate);
        int absStartx = absX - diagramRect.width / 2;
        JaretDate startDate = delegate.dateForCoordAbs(absStartx);
        tbv.setStartDate(startDate);
    }

    /**
     * Do the configuration of the properties that are the same for both time bar viewers.
     * 
     * @param tbv TimeBarViewer to configure
     */
    private void configureTBV(TimeBarViewer tbv) {
        // no selections
        tbv.getSelectionModel().setRowSelectionAllowed(false);
        tbv.getSelectionModel().setIntervalSelectionAllowed(false);
        tbv.getSelectionModel().setMultipleSelectionAllowed(false); // also disable rect selection

        // hide the y axis
        tbv.setYAxisWidth(0);

        // allow dragging to replace scrolling
        TimeScaleDragSupport tsds = new TimeScaleDragSupport(tbv, true);

        // use the box timescale renderer
        tbv.setTimeScaleRenderer(new BoxTimeScaleRenderer());

        // we will only render one row. This row should always be scaled to match the height of the diagram rectangle.
        // the row height strategy will ensure that
        tbv.getTimeBarViewState().setUseVariableRowHeights(true);
        tbv.getTimeBarViewState().setRowHeightStrategy(new IRowHeightStrategy() {

            public int calculateRowHeight(TimeBarViewerDelegate delegate, ITimeBarViewState timeBarViewState,
                    TimeBarRow row) {
                return delegate.getDiagramRect().height;
            }

            public boolean overrideDefault() {
                return true;
            }

        });

        // we use a simplified overlap strategy
        TimeBarViewerDelegate delegate = (TimeBarViewerDelegate) tbv.getData("delegate");
        tbv.setOverlapStrategy(new DistributeOverlapStrategy(delegate, NUMROWS));

    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(getClass().getName());
        shell.setSize(new Point(1000, 600));
    }

    public static void main(String[] args) {
        TimelineExample test = new TimelineExample();
        test.setBlockOnOpen(true);
        test.open();
    }
    
    public class DummyAction extends Action {
        private TimeBarViewer _tbv;

        public DummyAction(TimeBarViewer tbv) {
            _tbv = tbv;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            System.out.println("dummy action triggered");
        }

        /**
         * {@inheritDoc}
         */
        public String getText() {
            return "Dummy action";
        }

    }

}