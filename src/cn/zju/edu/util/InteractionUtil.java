package cn.zju.edu.util;

import java.util.HashMap;
import java.util.List;

import cn.zju.edu.blf.dao.LowLevelInteraction;

public class InteractionUtil {
	public final static HashMap<String,String> APP_MAP = new HashMap<String, String>();
	public final static HashMap<String, String> UI_ENG2CHS_MAP = new HashMap<String, String>();
	static
	{
		APP_MAP.put("firefox.exe", "Mozilla Firefox");
		APP_MAP.put("chrome.exe", "Google Chrome");
		APP_MAP.put("iexplore.exe", "Internet Explorer");
		APP_MAP.put("eclipse.exe", "Eclipse");
		APP_MAP.put("javaw.exe", "Eclipse");
		APP_MAP.put("devenv.exe", "Visual Studio");
		APP_MAP.put("WINWORD.exe", "Word Document");
		APP_MAP.put("notepad++.exe", "Notepad++");
		
		UI_ENG2CHS_MAP.put("button", "��ť");
		UI_ENG2CHS_MAP.put("edit", "�༭");
		UI_ENG2CHS_MAP.put("window", "����");
		UI_ENG2CHS_MAP.put("pane", "����");
		UI_ENG2CHS_MAP.put("dialog", "�Ի���");
		UI_ENG2CHS_MAP.put("tree item", "����Ŀ");
		UI_ENG2CHS_MAP.put("tab item", "ѡ���Ŀ");
		UI_ENG2CHS_MAP.put("split button", "��ְ�ť");
		UI_ENG2CHS_MAP.put("title bar", "������");
		UI_ENG2CHS_MAP.put("tool bar", "������");
		UI_ENG2CHS_MAP.put("menu item", "�˵���Ŀ");
		UI_ENG2CHS_MAP.put("tree", "��");
		UI_ENG2CHS_MAP.put("image", "ͼ��");
		UI_ENG2CHS_MAP.put("text", "�ĵ�");
		UI_ENG2CHS_MAP.put("thumb", "����ͼ");
		UI_ENG2CHS_MAP.put("check box", "��ѡ��");
		UI_ENG2CHS_MAP.put("list", "�б�");
		UI_ENG2CHS_MAP.put("list item", "�б���Ŀ");
		UI_ENG2CHS_MAP.put("tab", "Tab ��");
		UI_ENG2CHS_MAP.put("hyperlink", "��������");
	}
	
	public static boolean isControlType(String type, String value)
	{
		return type.equals(value) || UI_ENG2CHS_MAP.get(type).equals(value);
	}
	
	public static boolean isBrowser(String app)
	{
		return "firefox.exe".equals(app) || "chrome.exe".equals(app) || "iexplore.exe".equals(app);
	}
	
	public static String getWindowName(LowLevelInteraction u)
	{
		String app = u.getApplication();
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		else if("chrome.exe".equals(app))
		{
			return "Chrome Legacy Window".equals(u.getWindow()) || "".equals(u.getWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("WINWORD.EXE".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("devenv.exe".equals(app))
		{
			String w = "".equals(u.getWindow()) ? u.getParentUiName() : u.getWindow();
			String vsfile = getVSFile(u);
			if(!"".equals(vsfile)) 
			{
				return vsfile + " (" + w + ")";
			}
			
			return w;
		}
		else if("notepad++.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		
	}
	
	public static boolean isWebPage(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String appName = APP_MAP.get(u.getApplication());
		return w.endsWith(" - " + appName);
	}
	
	public static boolean isEclipseMainWindow(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class)\\s\\-\\sEclipse";
		return w.matches(pattern);
	}
	
	public static boolean isWinWORDFile(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		return w.contains("doc");
	}
	
	public static String getVSFile(LowLevelInteraction u)
	{
		if("Text Editor".equals(u.getUiName()))
		{
			String fileName = u.getParentUiName();
			if(fileName.contains(".cpp") || fileName.contains(".h"))
			{
				if(fileName.endsWith("*"))
				{
					fileName = fileName.substring(0, fileName.length()-1);
				}
				return fileName;
			}
		}
		else if("Solution Explorer".equals(u.getParentUiName()))
		{
			if(u.getUiName().contains(".cpp") || u.getUiName().contains(".h"))
			{
				return u.getUiName();
			}
		}
		
		return "";
	}
	
	public static boolean filterApplication(LowLevelInteraction u)

	
	{
		if("explorer.exe".equals(u.getApplication())) 
		{
			return true;
		}
		else if(isBrowser(u.getApplication()))
		{
			String title = getInteractionTitle(u);
			return !isWebPage(u) || title.equalsIgnoreCase("new tab");
		}
		else if("eclipse.exe".equals(u.getApplication()) || "javaw.exe".equals(u.getApplication()))
		{
			return  !isEclipseMainWindow(u);
		}
		else if("WINWORD.EXE".equals(u.getApplication()))
		{
			return !isWinWORDFile(u);
		}
		else if("devenv.exe".equals(u.getApplication()))
		{
			return "".equals(getVSFile(u));
		}
		
		return false;
	}
	
	public static boolean isAggregated(LowLevelInteraction u, LowLevelInteraction v) //u: previous, v: next
	{
		if(v.getApplication() != null && !v.getApplication().equals(u.getApplication())) return false;
		
		String app = u.getApplication();
		String w1 = getInteractionTitle(u);
		String w2 = getInteractionTitle(v);
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return isWebPage(u) && (w1.equals(w2) || "".equals(w2) || !isWebPage(v));
		}
		else if("chrome.exe".equals(app))
		{
			return w1.equals(w2) || "".equals(w2) || !isWebPage(v);
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return isEclipseMainWindow(u) && (w1.equals(w2) || "".equals(w2) || !isEclipseMainWindow(v));
		}
		else if("WINWORD.EXE".equals(app))
		{
			return isWinWORDFile(u) && (w1.equals(w2) || "".equals(w2) || !isWinWORDFile(v));
		}
		else if("devenv.exe".equals(app))
		{
			String f1 = getVSFile(u);
			String f2 = getVSFile(v);
			
			return !"".equals(f1) && (w1.equals(w2) || "".equals(f2));
			
		}
		else
		{
			return w1.equals(w2) || "".equals(w2);
		}
	}
	
	public static String getInteractionTitle(LowLevelInteraction u)
	{
		String title = "";
		
		String app = u.getApplication();
		title = getWindowName(u);
		if(isBrowser(app))
		{
			int index = title.indexOf(" - " + APP_MAP.get(app));
			if(index >= 0)
			{
				title = title.substring(0, index);
				
				if(isControlType("tab item", u.getUiType()))
				{
					title = u.getUiName();
				}
			}
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class)\\s\\-\\sEclipse";
			if(title.matches(pattern))
			{
				int index1 = title.lastIndexOf("/");
				int index2 = title.lastIndexOf(" - ");
				int index3 = title.indexOf(" - ");
				String fileName = title.substring(index1+1, index2);
				String pack = title.substring(index3+3, index1);
				title = fileName + "(" + pack + ")";
			}
		}
		
		return title;
	}
}
