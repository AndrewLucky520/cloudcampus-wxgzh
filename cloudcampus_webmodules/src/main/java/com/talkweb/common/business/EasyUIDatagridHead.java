package com.talkweb.common.business;
public class EasyUIDatagridHead {
	public String field;
    public String title;
    public String align;
    public int width;
    public int rowspan;
    public int colspan;
    public boolean sortable;
    public EasyUIDatagridHead(String _field, String _title, String _align, int _width, int _rowspan, int _colspan, boolean _sortable)
    {
        field = _field;
        title = _title;
        align = _align;
        width = _width;
        rowspan = _rowspan;
        colspan = _colspan;
        sortable = _sortable;
    }

}
