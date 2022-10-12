package com.eBrother.trans.image.util;

import com.sun.jna.Memory;

public class JnaMemory extends Memory {


    public JnaMemory ( long size ) {

        super ( size );
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
