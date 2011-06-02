/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.gwt.web.ui.asset.client.presenter;

import org.opennms.gwt.web.ui.asset.client.AssetPageConstants;
import org.opennms.gwt.web.ui.asset.client.AssetServiceAsync;
import org.opennms.gwt.web.ui.asset.client.event.SavedAssetEvent;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * 
 */
public class AssetPagePresenter implements Presenter {

	private AssetPageConstants con = GWT.create(AssetPageConstants.class);

	public interface Display {
		HasClickHandlers getSaveButton();

		HasClickHandlers getResetButton();

		void setData(AssetCommand asset);

		void setDataSugg(AssetSuggCommand assetSugg);

		AssetCommand getData();

		void setInfo(String info);

		void setEnable(Boolean enabled);

		void cleanUp();

		Widget asWidget();

		void setError(String description, Throwable throwable);

		boolean isUiValid();
	}

	private final AssetServiceAsync rpcService;
	private final HandlerManager eventBus;
	private final Display display;
	private AssetCommand asset;
	private AssetSuggCommand assetSugg;
	private int nodeId;

	public AssetPagePresenter(AssetServiceAsync rpcService, HandlerManager eventBus, Display view) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		this.display = view;
		
		try {
			nodeId = Integer.parseInt(Window.Location.getParameter("node"));
		} catch (NumberFormatException e) {
			GWT.log(con.nodeParamNotValidInt() + Window.Location.getParameter("node"), e);
			display.setError(con.nodeParamNotValidInt() + Window.Location.getParameter("node"), e);
		}
	}

	public void bind() {
		display.getSaveButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (display.isUiValid()) {
					display.setInfo(con.infoAssetSaving() + nodeId);
					saveAssetData();
				} else {
					GWT.log("isUiValid -> false; will not save.");
					display.setError(con.assetPageNotValidDontSave(), null);
				}
			}
		});

		display.getResetButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				display.setInfo(con.infoAssetRestting() + nodeId);
				display.setData(asset);
				display.cleanUp();
				display.setInfo(con.infoAsset() + nodeId);
			}
		});
	}

	public void go(final HasWidgets container) {
		bind();
		container.clear();
		container.add(display.asWidget());
		display.setInfo(con.infoAssetLoging() + nodeId);
		fetchAssetData();
	}

	private void fetchAssetData() {
		rpcService.getAssetByNodeId(nodeId, new AsyncCallback<AssetCommand>() {
			public void onSuccess(AssetCommand result) {
				asset = result;

				display.setData(asset);
				display.setInfo(con.infoAsset() + nodeId);
				display.cleanUp();
				fetchAssetSuggData();
			}

			public void onFailure(Throwable caught) {
				GWT.log(con.errorFatchingAssetData() + nodeId, caught);
				display.setError(con.errorFatchingAssetData() + nodeId, caught);
			}
		});
	}

	private void saveAssetData() {
		asset = display.getData();
		rpcService.saveOrUpdateAssetByNodeId(nodeId, asset, new AsyncCallback<Boolean>() {
			public void onSuccess(Boolean result) {
				eventBus.fireEvent(new SavedAssetEvent(nodeId));
				display.setInfo(con.infoAssetSaved() + nodeId);
				display.cleanUp();
				fetchAssetData();
			}

			public void onFailure(Throwable caught) {
				GWT.log(con.errorSavingAssetData() + nodeId, caught);
				display.setError(con.errorSavingAssetData() + nodeId, caught);
			}
		});
	}

	private void fetchAssetSuggData() {
		rpcService.getAssetSuggestions(new AsyncCallback<AssetSuggCommand>() {
			public void onSuccess(AssetSuggCommand result) {
				assetSugg = result;
				display.setDataSugg(assetSugg);
			}

			public void onFailure(Throwable caught) {
				GWT.log(con.errorFetchingAssetSuggData() + nodeId, caught);
				display.setError(con.errorFetchingAssetSuggData() + nodeId, caught);
			}
		});

	}
}