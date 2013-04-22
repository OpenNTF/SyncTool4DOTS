package biz.webgate.domino.synctool.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder.DominoViewCustomizer;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel.DynamicColumn;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.ColumnDef;

public class DynViewController extends DominoViewCustomizer {

	@Override
	public void afterCreateColumn(FacesContext context, int index,
			ColumnDef colDef, IControl column) {
		//Create a variable for the current component
	    UIComponent columnComponent = column.getComponent();
	    //Create a reference to the column and set the links to open in read mode
	    DynamicColumn dynamicColumn = (DynamicColumn) columnComponent;
	    dynamicColumn.setOpenDocAsReadonly(true);
		super.afterCreateColumn(context, index, colDef, column);
	}
}
