/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.erlide.basiccore.ErlLogger;
import org.erlide.basicui.util.IErlangStatusConstants;
import org.erlide.core.ErlangPlugin;
import org.erlide.core.erlang.ErlModelException;
import org.erlide.core.erlang.IErlElement;
import org.erlide.core.erlang.IErlFunction;
import org.erlide.core.erlang.IErlImport;
import org.erlide.core.erlang.IErlModule;
import org.erlide.core.erlang.IErlPreprocessorDef;
import org.erlide.core.erlang.ISourceReference;
import org.erlide.core.erlang.TokenWindow;
import org.erlide.core.util.ErlangFunction;
import org.erlide.core.util.ErlangIncludeFile;
import org.erlide.core.util.ResourceUtil;
import org.erlide.runtime.ErlangProjectProperties;
import org.erlide.runtime.backend.BackendManager;
import org.erlide.runtime.backend.IBackend;
import org.erlide.ui.ErlideUIPlugin;
import org.erlide.ui.editors.erl.ErlangEditor;
import org.erlide.ui.editors.util.EditorUtility;
import org.erlide.ui.util.ErlModelUtils;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import erlang.ErlideOpen;

/**
 * This action opens a Erlang editor on a Erlang element or file.
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>ICompilationUnit</code>, <code>IMember</code> or
 * <code>IFile</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class OpenAction extends SelectionDispatchAction {

	private ErlangEditor fEditor;
	private String fExternalModules;

	/**
	 * Creates a new <code>OpenAction</code>. The action requires that the
	 * selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 * @param externalModules
	 *            the externalModules file that can be searched for references
	 *            to external modules
	 */
	public OpenAction(IWorkbenchSite site, String externalModules) {
		super(site);
		fExternalModules = externalModules;
		setText(ActionMessages.OpenAction_label);
		setToolTipText(ActionMessages.OpenAction_tooltip);
		setDescription(ActionMessages.OpenAction_description);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "erl.open");

	}

	public OpenAction(ErlangEditor editor, String externalModules) {
		this(editor.getEditorSite(), externalModules);
		fEditor = editor;
		fExternalModules = externalModules;
		setText(ActionMessages.OpenAction_declaration_label);
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		for (final Iterator<?> iter = selection.iterator(); iter.hasNext();) {
			final Object element = iter.next();
			if (element instanceof ISourceReference) {
				continue;
			}
			if (element instanceof IFile) {
				continue;
			}
			if (element instanceof IStorage) {
				continue;
			}
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(ITextSelection selection) {
		ErlLogger.debug("*> goto " + selection);

		// if (!ActionUtil.isProcessable(getShell(), fEditor))
		// return;
		// try {
		// IErlElement element= SelectionConverter.codeResolve(fEditor,
		// getShell(),
		// getDialogTitle(),
		// ActionMessages.OpenAction_select_element);
		// if (element == null) {
		// IEditorStatusLine statusLine= (IEditorStatusLine)
		// fEditor.getAdapter(IEditorStatusLine.class);
		// if (statusLine != null)
		// statusLine.setMessage(true,
		// ActionMessages.OpenAction_error_messageBadSelection, null);
		// getShell().getDisplay().beep();
		// return;
		// }
		// IErlangElement input= SelectionConverter.getInput(fEditor);
		// int type= element.getElementType();
		// if (type == IErlangElement.Erlang_PROJECT || type ==
		// IErlangElement.PACKAGE_FRAGMENT_ROOT || type ==
		// IErlangElement.PACKAGE_FRAGMENT)
		// element= input;
		// run(new Object[] {element} );
		// } catch (ErlangModelException e) {
		// showError(e);
		// }
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection)) {
			return;
		}
		run(selection.toArray());
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param elements
	 *            the elements to process
	 */
	public void run(Object[] elements) {
		if (elements == null) {
			return;
		}
		for (Object element : elements) {
			try {
				element = getElementToOpen(element);
				final boolean activateOnOpen = fEditor != null ? true
						: OpenStrategy.activateOnOpen();
				open(element, activateOnOpen);
			} catch (final ErlModelException e) {
				ErlangPlugin.log(new Status(IStatus.ERROR,
						ErlangPlugin.PLUGIN_ID,
						IErlangStatusConstants.INTERNAL_ERROR,
						"OpenAction_error_message", e));

				ErrorDialog.openError(getShell(), getDialogTitle(),
						ActionMessages.OpenAction_error_messageProblems, e
								.getStatus());

			} catch (final PartInitException x) {

				String name = null;

				if (element instanceof IErlElement) {
					name = ((IErlElement) element).getElementName();
				} else if (element instanceof IStorage) {
					name = ((IStorage) element).getName();
				} else if (element instanceof IResource) {
					name = ((IResource) element).getName();
				}

				if (name != null) {
					MessageDialog.openError(getShell(),
							ActionMessages.OpenAction_error_messageProblems, x
									.getMessage());
				}
			}
		}
	}

	//
	// /**
	// * Opens the editor on the given element and subsequently selects it.
	// */
	// private static void open(Object element) throws ErlModelException,
	// PartInitException
	// {
	// open(element, true);
	// }

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param object
	 *            the element to open
	 * @return the real element to open
	 * @throws ErlangModelException
	 *             if an error occurs while accessing the Erlang model
	 */
	public Object getElementToOpen(Object object) throws ErlModelException {
		return object;
	}

	private String getDialogTitle() {
		return ActionMessages.OpenAction_error_title;
	}

	// private void showError(CoreException e)
	// {
	// ExceptionHandler.handle(e, getShell(), getDialogTitle(),
	// ActionMessages.OpenAction_error_message);
	// }

	@SuppressWarnings("boxing")
	@Override
	public void run() {
		int window = 15;

		fEditor = (ErlangEditor) getSite().getPage().getActiveEditor();
		final TokenWindow w = fEditor.getTokenWindow(window);
		if (w == null) {
			ErlLogger.debug("open::: null token window " + fEditor);
			return;
		}
		final OtpErlangList list = w.getList();
		window = w.getPos();
		final IBackend b = BackendManager.getDefault().getIdeBackend();
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IPathVariableManager pvm = workspace.getPathVariableManager();
			final String[] names = pvm.getPathVariableNames();
			final List<OtpErlangTuple> pathVars = new ArrayList<OtpErlangTuple>(
					names.length);
			for (final String name : names) {
				pathVars.add(new OtpErlangTuple(new OtpErlangString(name),
						new OtpErlangString(pvm.getValue(name).toOSString())));
			}
			final OtpErlangObject res = ErlideOpen.getOpenInfo(b, window, list,
					pathVars, fExternalModules);
			if (!(res instanceof OtpErlangTuple)) {
				return; // not a call, ignore
			}
			ErlLogger.debug("open res " + res);
			final OtpErlangTuple tres = (OtpErlangTuple) res;
			final String external = ((OtpErlangAtom) tres.elementAt(0))
					.atomValue();
			IProject project = null;
			final IErlModule module = ErlModelUtils.getModule(fEditor
					.getEditorInput());
			if (module != null) {
				project = module.getErlProject().getProject();
			}
			if (external.equals("external")) {
				final OtpErlangTuple mf = (OtpErlangTuple) tres.elementAt(1);
				final String mod = ((OtpErlangAtom) mf.elementAt(0))
						.atomValue();
				final String fun = ((OtpErlangAtom) mf.elementAt(1))
						.atomValue();
				final int arity = ((OtpErlangLong) mf.elementAt(2)).intValue();
				String path = null;
				if (mf.elementAt(3) instanceof OtpErlangString) {
					path = ((OtpErlangString) mf.elementAt(3)).stringValue();
				}
				openExternalFunction(mod, fun, arity, path);
			} else if (external.equals("include")) {
				final OtpErlangString s = (OtpErlangString) tres.elementAt(1);
				final String mod = s.stringValue();
				IResource r = ResourceUtil
						.recursiveFindNamedResourceWithReferences(project, mod);
				if (r == null) {
					try {
						final String m = findIncludeFile(project, mod, pvm);
						if (m != null) {
							r = EditorUtility.openExternal(m);
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
				if (r instanceof IFile) {
					final IFile f = (IFile) r;
					EditorUtility.openInEditor(f);
				}
			} else if (external.equals("local")) { // local call
				final OtpErlangTuple mf = (OtpErlangTuple) tres.elementAt(1);
				final String fun = ((OtpErlangAtom) mf.elementAt(0))
						.atomValue();
				final int arity = ((OtpErlangLong) mf.elementAt(1)).intValue();
				final IWorkbenchPage page = ErlideUIPlugin.getActivePage();
				if (page == null) {
					return;
				}
				final IEditorPart editor = page.getActiveEditor();
				if (!open(fun, arity, editor)) { // not local, imports
					if (module == null) {
						return;
					}
					final IErlImport ei = module.findImport(new ErlangFunction(
							fun, arity));
					if (ei == null) {
						return;
					}
					final String mod = ei.getImportModule();
					final OtpErlangObject res2 = ErlideOpen
							.getSourceFromModule(b, pathVars, mod,
									fExternalModules);
					if (res2 instanceof OtpErlangString) {
						final String path = ((OtpErlangString) res2)
								.stringValue();
						openExternalFunction(mod, fun, arity, path);
					}
				}
				// } else if (external.equals("variable")) {
				// final OtpErlangTuple mf = (OtpErlangTuple) tres.elementAt(1);
				// final OtpErlangAtom var = (OtpErlangAtom) mf.elementAt(0);
				// final ITextSelection sel = (ITextSelection) fEditor
				// .getSelectionProvider().getSelection();
				// final IErlElement e = fEditor.getElementAt(sel.getOffset(),
				// false);
				// final ISourceReference sref = (ISourceReference) e;
				// final OtpErlangString s = new
				// OtpErlangString(sref.getSource());
				// final OtpErlangObject res2 = b.rpcx("erlide_open",
				// "find_first_var", var, s);
				// if (!(res2 instanceof OtpErlangTuple)) {
				// return;
				// }
				// final OtpErlangTuple mf2 = (OtpErlangTuple) res2;
				// final OtpErlangTuple t = (OtpErlangTuple) mf2.elementAt(1);
				// final int pos = ((OtpErlangLong) t.elementAt(0)).intValue();
				// final int len = ((OtpErlangLong) t.elementAt(1)).intValue();
				// fEditor.setHighlightRange(pos
				// + sref.getSourceRange().getOffset(), len, true);
			} else if (external.equals("record") || external.equals("macro")) {
				final IWorkbenchPage page = ErlideUIPlugin.getActivePage();
				if (page == null) {
					return;
				}
				final boolean macro = external.equals("macro");
				final OtpErlangTuple mf = (OtpErlangTuple) tres.elementAt(1);
				final OtpErlangAtom defined = (OtpErlangAtom) mf.elementAt(0);
				final IErlModule m = ErlModelUtils.getModule(fEditor
						.getEditorInput());
				String definedName = defined.atomValue();
				if (definedName.length() == 0) {
					return;
				}
				if (definedName.charAt(0) == '?') {
					definedName = definedName.substring(1);
				}
				final IErlElement.ErlElementType type = macro ? IErlElement.ErlElementType.MACRO_DEF
						: IErlElement.ErlElementType.RECORD_DEF;
				openPreprocessorDef(project, page, m, definedName, type,
						new ArrayList<IErlModule>(), pvm);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param b
	 * @param project
	 * @param page
	 * @param m
	 * @param definedName
	 * @param type
	 * @throws CoreException
	 * @throws ErlModelException
	 * @throws PartInitException
	 */
	private boolean openPreprocessorDef(IProject project,
			final IWorkbenchPage page, IErlModule m, String definedName,
			final IErlElement.ErlElementType type,
			List<IErlModule> modulesDone, IPathVariableManager pvm)
			throws CoreException, ErlModelException, PartInitException {
		if (m == null) {
			return false;
		}
		modulesDone.add(m);
		m.open(null);
		final IErlPreprocessorDef pd = m.findPreprocessorDef(definedName, type);
		if (pd == null) {
			final ErlangIncludeFile[] includes = m.getIncludedFiles();
			for (final ErlangIncludeFile element : includes) {
				IResource re = ResourceUtil
						.recursiveFindNamedResourceWithReferences(project,
								element.getFilenameLastPart());
				if (re == null) {
					try {
						String s = element.getFilename();
						if (element.isSystemInclude()) {
							s = ErlideOpen.getIncludeLib(s);
						} else {
							s = findIncludeFile(project, s, pvm);
						}
						re = EditorUtility.openExternal(s);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
				if (re != null && re instanceof IFile) {
					m = ErlModelUtils.getModule((IFile) re);
					if (m != null && !modulesDone.contains(m)) {
						if (openPreprocessorDef(project, page, m, definedName,
								type, modulesDone, pvm)) {
							return true;
						}
					}
				}
			}
		}
		if (pd != null) {
			final IEditorPart editor = EditorUtility.openInEditor(m);
			EditorUtility.revealInEditor(editor, pd);
			return true;
		}
		return false;
	}

	private String findIncludeFile(IProject project, String s,
			IPathVariableManager pvm) {
		final ErlangProjectProperties prefs = new ErlangProjectProperties(
				project);
		for (final String includeDir : prefs.getIncludeDirs()) {
			IPath p = new Path(includeDir).append(s);
			p = pvm.resolvePath(p);
			final File f = new File(p.toOSString());
			if (f.exists()) {
				return p.toString();
			}
		}
		return null;
	}

	/**
	 * Open an editor on the given module and select the given erlang function
	 * 
	 * @param mod
	 *            module name (without .erl)
	 * @param fun
	 *            function name
	 * @param arity
	 *            function arity
	 * @param path
	 *            path to module (including .erl)
	 * @throws CoreException
	 */
	private void openExternalFunction(String mod, String fun, int arity,
			String path) throws CoreException {
		final String modFileName = mod + ".erl";
		ErlLogger.debug("open ex mod" + modFileName);
		final IErlModule m = ErlModelUtils.getModule(fEditor.getEditorInput());
		IResource r = null;
		if (m != null) {
			final IProject p = m.getErlProject().getProject();
			if (p != null) {
				r = ResourceUtil.recursiveFindNamedResourceWithReferences(p,
						modFileName);
			}
		}
		if (r == null) {
			try {
				r = EditorUtility.openExternal(path);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if (r != null && r instanceof IFile) {
			final IFile f = (IFile) r;
			try {
				final IEditorPart editor = EditorUtility.openInEditor(f);
				open(fun, arity, editor);
			} catch (final PartInitException e) {
				e.printStackTrace();
			} catch (final ErlModelException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Activate editor and select erlang function
	 * 
	 * @param fun
	 * @param arity
	 * @param editor
	 * @throws ErlModelException
	 */
	private static boolean open(String fun, int arity, IEditorPart editor)
			throws ErlModelException {
		if (editor == null) {
			return false;
		}
		final IErlModule m = ErlModelUtils.getModule(editor.getEditorInput());
		ErlLogger.debug("open fun m" + fun + "/" + arity + " " + m);
		if (m == null) {
			return false;
		}
		m.open(null); // FIXME vi m�ste kolla s� att den inte
		// dubbel-parsas... kanske s�tta n�n flagga p�
		// modulen? funkar isStructureKnown() ??
		final IErlFunction function = ErlModelUtils.findFunction(m, fun, arity);
		if (function == null) {
			return false;
		}
		EditorUtility.revealInEditor(editor, function);
		return true;
	}

	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	private static void open(Object element, boolean activate)
			throws ErlModelException, PartInitException {
		final IEditorPart part = EditorUtility.openInEditor(element, activate);
		if (element instanceof IErlElement) {
			EditorUtility.revealInEditor(part, (IErlElement) element);
		}
	}
}
