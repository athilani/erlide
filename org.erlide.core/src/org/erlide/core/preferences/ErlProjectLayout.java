package org.erlide.core.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.erlide.jinterface.util.Bindings;
import org.erlide.jinterface.util.ErlUtils;
import org.erlide.jinterface.util.ParserException;

import com.ericsson.otp.erlang.OtpErlang;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.google.common.collect.Lists;

/*
 * Note: in paths, first segment in all caps is taken as a path variable when 
 * converting to Erlang term
 */

public class ErlProjectLayout {

    private final List<IPath> sources;
    private final List<IPath> includes;
    private final IPath output;
    private final List<IPath> docs;
    private final IPath priv;

    public final static ErlProjectLayout OTP_LAYOUT = new ErlProjectLayout(
            Lists.newArrayList((IPath) new Path("src")),
            Lists.newArrayList((IPath) new Path("include")), new Path("ebin"),
            Lists.newArrayList((IPath) new Path("doc")), new Path("priv"));

    public ErlProjectLayout(List<IPath> sources, List<IPath> includes,
            IPath output, List<IPath> docs, IPath priv) {
        this.sources = sources;
        this.includes = includes;
        this.output = output;
        this.docs = docs;
        this.priv = priv;
    }

    public ErlProjectLayout(OtpErlangObject layout) throws ParserException,
            OtpErlangException {
        Bindings b = ErlUtils.match("{layout,S,I,E,D,P}", layout);
        Collection<OtpErlangObject> s = b.getList("S");
        Collection<OtpErlangObject> i = b.getList("I");
        OtpErlangObject e = b.get("E");
        Collection<OtpErlangObject> d = b.getList("D");
        OtpErlangObject p = b.get("P");

        sources = mkList(s);
        includes = mkList(i);
        output = mkPath(e);
        docs = mkList(d);
        priv = mkPath(p);
    }

    public Collection<IPath> getSources() {
        return Collections.unmodifiableCollection(sources);
    }

    public Collection<IPath> getIncludes() {
        return Collections.unmodifiableCollection(includes);
    }

    public IPath getOutput() {
        return output;
    }

    public Collection<IPath> getDocs() {
        return Collections.unmodifiableCollection(docs);
    }

    public IPath getPriv() {
        return priv;
    }

    public OtpErlangObject asTerm() {
        OtpErlangObject s = listAsTerm(sources);
        OtpErlangObject i = listAsTerm(includes);
        OtpErlangObject o = pathAsTerm(output);
        OtpErlangObject d = listAsTerm(docs);
        OtpErlangObject p = pathAsTerm(priv);

        return OtpErlang.mkTuple(new OtpErlangAtom("layout"), s, i, o, d, p);
    }

    public ErlProjectLayout setSources(Collection<IPath> list) {
        List<IPath> list1 = Lists.newArrayList(list);
        ErlProjectLayout result = new ErlProjectLayout(list1, includes, output,
                docs, priv);
        return result;
    }

    public ErlProjectLayout addSource(String string) {
        return addSource(new Path(string));
    }

    public ErlProjectLayout addSource(IPath path) {
        List<IPath> sources1 = addToListIfNotExisting(sources, path);
        return setSources(sources1);
    }

    public ErlProjectLayout addSources(Collection<IPath> paths) {
        List<IPath> sources1 = Lists.newArrayList();
        for (IPath p : paths) {
            sources1.add(p);
        }
        return setSources(sources1);
    }

    public ErlProjectLayout setIncludes(Collection<IPath> list) {
        List<IPath> list1 = Lists.newArrayList(list);
        ErlProjectLayout result = new ErlProjectLayout(sources, list1, output,
                docs, priv);
        return result;
    }

    public ErlProjectLayout addInclude(String string) {
        return addInclude(new Path(string));
    }

    public ErlProjectLayout addInclude(IPath path) {
        List<IPath> list = addToListIfNotExisting(includes, path);
        return setIncludes(list);
    }

    public ErlProjectLayout setOutput(String string) {
        return setOutput(new Path(string));
    }

    public ErlProjectLayout setOutput(Path path) {
        ErlProjectLayout result = new ErlProjectLayout(sources, includes, path,
                docs, priv);
        return result;
    }

    public ErlProjectLayout setDocs(Collection<IPath> list) {
        List<IPath> list1 = Lists.newArrayList(list);
        ErlProjectLayout result = new ErlProjectLayout(sources, includes,
                output, list1, priv);
        return result;
    }

    public ErlProjectLayout addDoc(String string) {
        return addDoc(new Path(string));
    }

    public ErlProjectLayout addDoc(IPath path) {
        List<IPath> list = addToListIfNotExisting(docs, path);
        ErlProjectLayout result = new ErlProjectLayout(sources, includes,
                output, list, priv);
        return result;
    }

    private List<IPath> addToListIfNotExisting(Collection<IPath> list,
            IPath path) {
        List<IPath> result = Lists.newArrayList(list);
        if (!result.contains(path)) {
            result.add(path);
        }
        return result;
    }

    public ErlProjectLayout setPriv(String string) {
        return setPriv(new Path(string));
    }

    public ErlProjectLayout setPriv(Path path) {
        ErlProjectLayout result = new ErlProjectLayout(sources, includes,
                output, docs, path);
        return result;
    }

    private List<IPath> mkList(Collection<OtpErlangObject> s)
            throws OtpErlangException {
        if (s == null) {
            return null;
        }
        List<IPath> result = Lists.newArrayList();
        for (OtpErlangObject o : s) {
            result.add(mkPath(o));
        }
        return result;
    }

    private IPath mkPath(OtpErlangObject p) throws OtpErlangException {
        if (p instanceof OtpErlangString) {
            return new Path(((OtpErlangString) p).stringValue());
        } else if (p instanceof OtpErlangTuple) {
            try {
                Bindings b = ErlUtils.match("{V,P}", p);
                String v = b.getAtom("V");
                String path = b.getString("P");
                return new Path(v).append(new Path(path));
            } catch (ParserException e) {
                return null;
            }
        }
        return null;
    }

    private OtpErlangObject listAsTerm(List<IPath> list) {
        List<OtpErlangObject> result = Lists.newArrayList();
        for (IPath p : list) {
            result.add(pathAsTerm(p));
        }
        return OtpErlang.mkList(result);
    }

    private OtpErlangObject pathAsTerm(IPath path) {
        String first = path.segment(0);
        if (first.equals(first.toUpperCase())) {
            return OtpErlang
                    .mkTuple(new OtpErlangAtom(path.segment(0)),
                            new OtpErlangString(path.removeFirstSegments(1)
                                    .toString()));
        }
        return new OtpErlangString(path.toString());
    }

    public ErlProjectLayout removeSource(String string) {
        return removeSource(new Path(string));
    }

    public ErlProjectLayout removeSource(Path path) {
        List<IPath> list = removeFromList(sources, path);
        ErlProjectLayout result = new ErlProjectLayout(list, includes, output,
                docs, priv);
        return result;
    }

    public ErlProjectLayout removeInclude(String string) {
        return removeInclude(new Path(string));
    }

    public ErlProjectLayout removeInclude(Path path) {
        List<IPath> list = removeFromList(includes, path);
        ErlProjectLayout result = new ErlProjectLayout(sources, list, output,
                docs, priv);
        return result;
    }

    public ErlProjectLayout removeDoc(String string) {
        return removeDoc(new Path(string));
    }

    public ErlProjectLayout removeDoc(Path path) {
        List<IPath> list = removeFromList(docs, path);
        ErlProjectLayout result = new ErlProjectLayout(sources, includes,
                output, list, priv);
        return result;
    }

    private List<IPath> removeFromList(List<IPath> list, Path path) {
        List<IPath> result = Lists.newArrayList(list);
        result.remove(path);
        return result;
    }

}
