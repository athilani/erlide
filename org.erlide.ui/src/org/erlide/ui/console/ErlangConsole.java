/*******************************************************************************
 * Copyright (c) 2009 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available
 * at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.console;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;
import org.erlide.jinterface.backend.Backend;
import org.erlide.jinterface.backend.BackendShell;
import org.erlide.runtime.backend.ErlideBackend;

public class ErlangConsole extends TextConsole {
	private final BackendShell shell;
	protected ListenerList consoleListeners;
	protected ErlangConsolePartitioner partitioner;
	private boolean stopped = false;

	public ErlangConsole(ErlideBackend backend) {
		super(backend.getName(), null, null, true);
		shell = backend.getShell("main");
		this.consoleListeners = new ListenerList(ListenerList.IDENTITY);

		partitioner = new ErlangConsolePartitioner();
		getDocument().setDocumentPartitioner(partitioner);
		partitioner.connect(getDocument());
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		return new ErlangConsolePage(view, this);
	}

	public Backend getBackend() {
		return shell.getBackend();
	}

	public BackendShell getShell() {
		return shell;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Erlang: " + shell.getBackend().getInfo().toString() + " "
				+ shell.hashCode();
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	}

	public void show() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(id);
			view.display(this);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected IConsoleDocumentPartitioner getPartitioner() {
		return partitioner;
	}

	public void stop() {
		stopped  = true;
	}

	public boolean isStopped() {
		return stopped;
	}

}
