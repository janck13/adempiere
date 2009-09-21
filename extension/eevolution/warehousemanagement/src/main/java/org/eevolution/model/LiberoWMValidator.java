/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *                 Teo Sarca, www.arhipac.ro                                  *
 *****************************************************************************/
package org.eevolution.model;

import java.util.Collection;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MForecastLine;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MWarehouse;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_M_Forecast;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;


/**
 * Libero Validator 
 *	
 * @author Victor Perez
 * @author Trifon Trifonov
 *		<li>[ 2270421 ] Can not complete Shipment (Customer)</li>
 * @author Teo Sarca, www.arhipac.ro
 */
public class LiberoWMValidator implements ModelValidator
{
	/** Context variable which says if libero manufacturing is enabled */
	public static final String CTX_IsLiberoEnabled = "#IsLiberoEnabled";
	
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	
	
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		if (client != null)
		{	
			m_AD_Client_ID = client.getAD_Client_ID();
		}
		engine.addModelChange(MDDOrderLine.Table_Name, this);
		//engine.addDocValidate(MDDOrderLine.Table_Name, this);
		//engine.addDocValidate(MMovement.Table_Name, this);
	}	//	initialize

	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		if (po instanceof MDDOrderLine && (TYPE_AFTER_CHANGE == type && po.is_ValueChanged(MDDOrderLine.COLUMNNAME_QtyDelivered)))
		{
			MDDOrderLine oline = (MDDOrderLine)po;
			int WM_InOutBoundLine_ID = (Integer) oline.get_Value(MWMInOutBoundLine.COLUMNNAME_WM_InOutBoundLine_ID);
		
			if(WM_InOutBoundLine_ID > 0 && oline.getQtyOrdered().compareTo(oline.getQtyDelivered()) >= 1)
			{
					
					MWMInOutBoundLine obline = new MWMInOutBoundLine(oline.getCtx(),WM_InOutBoundLine_ID, oline.get_TrxName());
					obline.setPickedQty(oline.getQtyDelivered());
					obline.saveEx();	
			}
		}
			return null;
	}	//	modelChange
	
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		return null;
	}	//	docValidate
	
	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		Env.setContext(Env.getCtx(), CTX_IsLiberoEnabled, true);
		return null;
	}	//	login

	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID
}	//	LiberoValidator
