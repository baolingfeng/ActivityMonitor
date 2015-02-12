package cn.zju.edu.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import cn.zju.edu.blf.dao.ActivityObject;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.manager.*;
import cn.zju.edu.util.*;
import cn.zju.edu.swt.table.*;

public class CurrentActivityPane extends JPanel implements ActionListener{
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode node1;
	protected DefaultMutableTreeNode node2;
	protected DefaultMutableTreeNode node3;
	protected DefaultMutableTreeNode node4;
	
	protected HashMap<String, DefaultMutableTreeNode> appNodes;
	
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	
	protected CurrentActivityManager manager = new CurrentActivityManager();
	protected HistoryActivityManager hist;
	
	private Thread internalThread;
	
	public CurrentActivityPane() throws Exception
	{
	    super(new BorderLayout());
	    
	    rootNode = new DefaultMutableTreeNode("Root Node");
	    treeModel = new DefaultTreeModel(rootNode);
	    
	    tree = new JTree(treeModel);
	    tree.setRootVisible(false);
	    tree.setCellRenderer(new ActivityTreeCellRenderer());
	   
	    node1 = new DefaultMutableTreeNode("Web page");
	    node2 = new DefaultMutableTreeNode("Eclipse");
	    node3 = new DefaultMutableTreeNode("Visual Studio");
	    node4 = new DefaultMutableTreeNode("Document");
	   
	    rootNode.add(node1);
	    rootNode.add(node2);
	    rootNode.add(node3);
	    rootNode.add(node4);
	    
	    addContextMenu();
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane, BorderLayout.CENTER);
	    
	    Runnable r = new Runnable() {
		      public void run() {
		        try {
		          runWork();
		        } catch (Exception x) {
		          x.printStackTrace();
		        }
		      }
		 };

	    internalThread = new Thread(r, "current activity");
	    internalThread.start();
	    
	}
	
	@SuppressWarnings("rawtypes")
	public void createNodes()
	{
		node1.removeAllChildren();
		node2.removeAllChildren();
		node3.removeAllChildren();
		node4.removeAllChildren();
		
		List<ActivityObject> list = manager.getOrderedMap();
		
		for(int i=0; i<list.size(); i++)
		{
			ActivityObject a = list.get(i);
			if(InteractionUtil.isBrowser(a.getApplication()))
			{
				if(FilterManager.getInstance().isFilter(a.getTitle())) continue;
				
				 treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node1, node1.getChildCount());
			}
			else if("eclipse.exe".equals(a.getApplication()) || "javaw.exe".equals(a.getApplication()))
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node2, node2.getChildCount());
			}
			else if("devenv.exe".equals(a.getApplication()))
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node3, node3.getChildCount());
			}
			else
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node4, node4.getChildCount());
			}
		}
		
		treeModel.reload();
		for(int i=0; i<tree.getRowCount(); i++)
		{
			tree.expandRow(i);
		}
		
	    
	}
	
	public void runWork()
	{
		try
		{
			if(manager.retrieveInteractions())
			{
				createNodes();
			}
			
			int i = 0;
			while(true)
			{
				Thread.sleep(5 * 1000);
				
				if(manager.retrieveInteractions())
				{
					createNodes();
				}
				
				i++;
				this.repaint();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	 class ActivityTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

	        ActivityTreeCellRenderer() {
	        }

	        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	            
	        	 super.getTreeCellRendererComponent(tree, value, selected, expanded,
                         leaf, row, hasFocus);
	        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	        	 
	        	Object o = node.getUserObject();
	            if (o instanceof ActivityObject) {
	            	try
	            	{
		            	ActivityObject a = (ActivityObject) o;
		            	Icon icon = IconManager.getIcon(a.getApplication());
		            	if(icon == null)
		            	{
		            		icon = IconManager.getIcon("default");
		            	}
		            	
		            	this.setIcon(icon);
		            	this.setText(a.getTitle());
		            	if(HistoryActivityManager.getInstance().hasGroup(a.getTitle(), a.getApplication()))
		            	{
		            		this.setForeground(Color.BLUE);
		            		//this.setBackground(Color.BLUE);
		            	}
	            	}catch(Exception e)
	            	{
	            		e.printStackTrace();
	            	}
	            	
	            } else if(node.getParent() == tree.getModel().getRoot())
	            {
	            	Icon icon = IconManager.getIcon(node.toString());
	            	this.setIcon(icon);
	            	this.setText("" + value);
	            }	
	            else	
	            {
	            	this.setOpenIcon(this.getDefaultOpenIcon());
	            	this.setClosedIcon(this.getDefaultClosedIcon());
	                this.setText("" + value);
	            }
	            return this;
	        }
	    }
	 
	 public void addContextMenu()
	 {
		 final JPopupMenu popup = new JPopupMenu();
		 JMenuItem mi = new JMenuItem("Show History Interactions");
		 mi.addActionListener(this);
		 mi.setActionCommand("history");
		 popup.add(mi);
		 
		 mi = new JMenuItem("Show Screen-Captured Image");
		 mi.addActionListener(this);
		 mi.setActionCommand("screen");
		 popup.add(mi);
		 
		 final JPopupMenu popup2 = new JPopupMenu();
		 mi = new JMenuItem("Show Code History");
		 mi.addActionListener(this);
		 mi.setActionCommand("javacode");
		 popup2.add(mi);
		 
		 mi = new JMenuItem("Show Interaction History");
		 mi.addActionListener(this);
		 mi.setActionCommand("history");
		 popup2.add(mi);
		 
		 mi = new JMenuItem("Show Screen-Captured Image");
		 mi.addActionListener(this);
		 mi.setActionCommand("screen");
		 popup2.add(mi);
		 
		 tree.addMouseListener(new MouseAdapter() {
	            public void mouseReleased( MouseEvent e )
	            {
	            	TreePath path = tree.getSelectionPath();
	        		if(path == null) return;
	        		
	             	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
	             	Object o = dmtn.getUserObject();
	            	if(o instanceof ActivityObject)
	            	{
	            		ActivityObject a = (ActivityObject)o;
	            		try
	            		{
	            			if(!HistoryActivityManager.getInstance().hasGroup(a.getTitle(), a.getApplication())) return;
	            			
	            			if("eclipse.exe".equals(a.getApplication()) || "javaw.exe".equals(a.getApplication()))
	            			{
	            				if (e.isPopupTrigger()) {
	        	                    popup2.show( (JComponent)e.getSource(), e.getX(), e.getY() );
	        	                    return;
	        	                }
	            			}
	            		}
	            		catch(Exception exp)
	            		{
	            			exp.printStackTrace();
	            		}
	            		
	            	}
	            	else 
	            	{
	            		return;
	            	}
	             	
	                if (e.isPopupTrigger()) {
	                    popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
	                }
	            }
		 });
	 }
	 
	 public void actionPerformed(ActionEvent ae) {
		TreePath path = tree.getSelectionPath();
		if(path == null) return;
		
     	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
     	Object o = dmtn.getUserObject();
     	ActivityObject a = null;
     	if(o instanceof ActivityObject)
     	{
     		a = (ActivityObject)o;
     	}
     	
     	try
     	{
	        if (ae.getActionCommand().equals("history")) {
        			
        			ActivityDetailWindow aw = new ActivityDetailWindow(a.getTitle(), a.getApplication());
        			aw.show();
	        }
	        else if(ae.getActionCommand().equals("screen"))
	        {
    			ActivityScreenWindow aw = new ActivityScreenWindow(a.getTitle(), a.getApplication());
    			aw.setVisible(true);
	        }
	        else if(ae.getActionCommand().equals("javacode"))
	        {
	        	final String title = a.getTitle();
	        	final String app = a.getApplication();
	        	
	        	CodeChangeTable table = new CodeChangeTable(null,title, app);
	        	
	        	/*
	        	 Runnable r = new Runnable() {
		   		      public void run() {
		   		        try {
		   		        	CodeChangeTable table = new CodeChangeTable(null,title, app);
		   		        } catch (Exception x) {
		   		          x.printStackTrace();
		   		        }
		   		      }
	        	 };

	   	    Thread athread = new Thread(r, "athread");
	   	    athread.start();
	        	*/
	        }
     	}catch(Exception e)
     	{
     		e.printStackTrace();
     	}
	}
}
