/*******************************************************************************
 * Copyright (c) 2004 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/

package org.erlide.runtime.backend.internal;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.erlide.runtime.backend.ErtsProcess;

public class ErtsProcessFactory implements IProcessFactory {

	public static final String ID = "org.erlide.core.backend.ertsprocessfactory";

	public IProcess newProcess(final ILaunch launch, final Process process,
			final String label, @SuppressWarnings("rawtypes") final Map attributes) {
		// ErlLogger.debug("#* ProcFact: " + label + " " + attributes);

		return new ErtsProcess(launch, process, label, attributes);
	}

}
