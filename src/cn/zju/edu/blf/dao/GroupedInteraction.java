package cn.zju.edu.blf.dao;

import java.util.ArrayList;
import java.util.List;

import cn.zju.edu.util.DateUtil;

public class GroupedInteraction implements Comparable<GroupedInteraction>{
	private int groupId;
	private String title;
	private String application;
	private double duration;
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	//private List<String> times;
	private List<GroupDetail> details;
	
	public GroupedInteraction()
	{
		groupId = -1;
		title = "";
		application = "";
		//times = new ArrayList<String>();
		details = new ArrayList<GroupDetail>();
	}
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	
	public List<GroupDetail> getDetails() {
		return details;
	}

	public void setDetails(List<GroupDetail> details) {
		this.details = details;
	}

	public void addDetail(GroupDetail detail)
	{
		details.add(detail);
	}
	
	public void addDetail(List<GroupDetail> list)
	{
		details.addAll(list);
	}
	
	public int hashCode()
	{
		return (title+"#"+application).hashCode();
	}
	
	public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        GroupedInteraction other = (GroupedInteraction) obj;
	        if (!title.equals(other.getTitle()) || !application.equals(other.getApplication()))
	            return false;
	        return true;
	    }
	 
	@Override
	public int compareTo(GroupedInteraction a) 
	{
		if(duration - a.getDuration() > 0)
		{
			return 1;
		}
		else if(duration - a.getDuration() < 0)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}	
}
