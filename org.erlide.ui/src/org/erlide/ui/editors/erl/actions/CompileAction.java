/*******************************************************************************
 * Copyright (c) 2008 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.editors.erl.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchSite;
import org.erlide.core.builder.BuildResource;
import org.erlide.core.builder.BuilderHelper;
import org.erlide.core.builder.CompilerPreferences;
import org.erlide.core.erlang.ErlangCore;
import org.erlide.core.erlang.IErlModule;
import org.erlide.core.erlang.IOldErlangProjectProperties;
import org.erlide.jinterface.backend.Backend;
import org.erlide.ui.editors.erl.ErlangEditor;
import org.osgi.service.prefs.BackingStoreException;

import com.ericsson.otp.erlang.OtpErlangList;

public class CompileAction extends Action {

    private final IWorkbenchSite site;
    private final BuilderHelper helper = new BuilderHelper();

    public CompileAction(final IWorkbenchSite site) {
        super("Compile file");
        this.site = site;
    }

    @Override
    public void run() {
        final ErlangEditor editor = (ErlangEditor) getSite().getPage()
                .getActiveEditor();
        final IErlModule module = editor.getModule();
        if (module == null) {
            return;
        }
        final Backend b = ErlangCore.getBackendManager().getIdeBackend();

        final IResource resource = module.getResource();
        final IProject project = resource.getProject();
        BuildResource bres = new BuildResource(resource);
        CompilerPreferences prefs = new CompilerPreferences(project);
        try {
            prefs.load();
        } catch (BackingStoreException e1) {
            e1.printStackTrace();
        }
        OtpErlangList compilerOptions = prefs.export();
        final IOldErlangProjectProperties pprefs = ErlangCore
                .getProjectProperties(project);

        if ("erl".equals(resource.getFileExtension())) {
            helper.compileErl(project, bres, pprefs.getOutputDir()
                    .toString(), b, compilerOptions);
        }
        if ("yrl".equals(resource.getFileExtension())) {
            helper.compileYrl(project, bres, b, compilerOptions);
        }
    }

    public IWorkbenchSite getSite() {
        return site;
    }
}
