/*
 * Created on 14.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.inet.jortho;

import java.awt.Toolkit;

import javax.swing.*;

import junit.framework.TestCase;

public class MemoryTest extends TestCase {
    
    static{
        AllTests.init();
    }
    
    /**
     * Create a large amount of languages menus
     */
    public void testCreateLanguagesMenu() throws Exception{
        SpellChecker.createLanguagesMenu();
        
        long memoryBefore = usedMemory();
        
        for( int i=0; i< 10000; i++ ){
            SpellChecker.createLanguagesMenu();
        }
        
        long memoryAfter = usedMemory();
        
        if (memoryBefore + 100000 < memoryAfter ){
            fail("Memory Leak SpellChecker.createLanguagesMenu. memory before:" + (memoryBefore/1024) + " KB  memory after:" + (memoryAfter/1024) +" KB" );
        }
    }

    /**
     * Create many JTextPane and register the spell checker
     */
    public void testRegister() throws Exception{
        // Create a large text
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<1000; i++){
            buf.append( "This is a very simple sentence.\n" );
        }
        String text = buf.toString();
        buf = null;
        
        JTextPane textPane1 = new JTextPane();
        textPane1.setText( text );
        SpellChecker.register( textPane1 );
        textPane1 = null;
        
        long memoryBefore = usedMemory();
        
        for( int i=0; i< 100; i++ ){
            JTextPane textPane = new JTextPane();
            textPane.setText( text );
            SpellChecker.register( textPane );
            // there will be some thread started, we give it a little time
            System.err.println(usedMemory());
            Thread.sleep( 10 );
        }
        
        long memoryAfter = usedMemory();
        
        if (memoryBefore + 1000000 < memoryAfter ){
            fail("Memory Leak SpellChecker.register. memory before:" + (memoryBefore/1024) + " KB  memory after:" + (memoryAfter/1024) +" KB" );
        }
    }

    /**
     * Start the gc and caluculate the the current used memory.
     */
    private long usedMemory() throws Exception{
        Runtime runtime = Runtime.getRuntime();
        long last = Long.MAX_VALUE;
        while(true){
            Thread.sleep( 1 );
            // empty the event loop, because it hold many references
            if( Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() != null ){
                continue;
            }
            System.runFinalization();
            System.gc();
            Thread.sleep( 10 );
            long current = runtime.totalMemory() - runtime.freeMemory();
            if(current < last){
                // if the value are reduced then wait for more reducing
                last = current;
            } else {
                return last;
            }
        }
    }
}
