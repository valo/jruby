/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.cast.ToSNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyString;
import org.jruby.util.ByteList;

/**
 * A list of expressions to build up into a string.
 */
public final class InterpolatedStringNode extends RubyNode {

    @Children protected final ToSNode[] children;

    public InterpolatedStringNode(RubyContext context, SourceSection sourceSection, ToSNode[] children) {
        super(context, sourceSection);
        this.children = children;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        final RubyString[] strings = new RubyString[children.length];

        for (int n = 0; n < children.length; n++) {
            strings[n] = children[n].executeString(frame);
        }

        return concat(strings);
    }

    @CompilerDirectives.TruffleBoundary
    private RubyString concat(RubyString[] strings) {
        // TODO(CS): what happens to encoding here

        int length = 0;

        for (RubyString string : strings) {
            length += string.getBytes().length();
        }

        final ByteList bytes = new ByteList(length);

        for (RubyString string : strings) {
            bytes.append(string.getBytes());
        }

        return new RubyString(getContext().getCoreLibrary().getStringClass(), bytes);
    }

    @ExplodeLoop
    @Override
    public void executeVoid(VirtualFrame frame) {
        for (int n = 0; n < children.length; n++) {
            children[n].executeVoid(frame);
        }
    }

}
