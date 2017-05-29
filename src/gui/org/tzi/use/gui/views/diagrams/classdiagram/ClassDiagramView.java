/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// $Id: ClassDiagramView.java 1050 2009-07-07 16:25:22Z lars $

package org.tzi.use.gui.views.diagrams.classdiagram;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tzi.use.graph.DirectedGraph;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.main.ModelBrowser;
import org.tzi.use.gui.views.PrintableView;
import org.tzi.use.gui.views.View;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassifier;
import org.tzi.use.uml.mm.MGeneralization;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.commonbehavior.communications.MSignal;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.sys.MSystem;

/**
 * A graph showing an class diagram with all elements in the
 * current instance of MModel
 *
 * @version $ProjectVersion: 0.393 $
 * @author Fabian Gutsche
 * */
@SuppressWarnings("serial")
public class ClassDiagramView extends JPanel 
                                 implements View, 
                                            PrintableView {

    private final MainWindow fMainWindow;
    
    private final MSystem fSystem;
    private final MSystem fMetaSystem;
    
    private ClassDiagram fClassDiagram;
/*
    public ClassDiagramView( MainWindow mainWindow, MSystem system, boolean loadLayout ) { 
    	this.setFocusable(true);
        fMainWindow = mainWindow;
        fSystem = system;
        setLayout( new BorderLayout() );
        initDiagram(loadLayout, null, false);
    }
    */
    public ClassDiagramView( MainWindow mainWindow, MSystem system, MSystem metaSystem, boolean loadLayout, boolean isMetamodel) { 
    	this.setFocusable(true);
        fMainWindow = mainWindow;
        fSystem = system;
        fMetaSystem = metaSystem;
        setLayout( new BorderLayout() );
        initDiagram(loadLayout, null, isMetamodel);
    }

	public void initDiagram(boolean loadDefaultLayout, ClassDiagramOptions opt, boolean isMetamodel) {
		if (opt == null)
			fClassDiagram = new ClassDiagram( this, fMainWindow.logWriter());
		else
			fClassDiagram = new ClassDiagram( this, fMainWindow.logWriter(), new ClassDiagramOptions(opt));
		
		fClassDiagram.setStatusBar(fMainWindow.statusBar());
		this.removeAll();
        add( new JScrollPane(fClassDiagram) );
		
        initState(isMetamodel);
        
        if (loadDefaultLayout && !isMetamodel) {
        	fClassDiagram.loadDefaultLayout();
        }
	}
    
    public MSystem system() {
        return fSystem;
    }
    
    public MSystem metaSystem() {
        return fMetaSystem;
    }
    
    public ClassDiagram classDiagram()
    {
    	return fClassDiagram;
    }
    /**
     * Returns the model browser.
     */
    public ModelBrowser getModelBrowser() {
        return fMainWindow.getModelBrowser();
    }
    
    /**
     * Determines if this is the selected view.
     * @return <code>true</code> if it is the selected view, otherwise
     * <code>false</false>
     */
    public boolean isSelectedView() {
        if ( fMainWindow.getSelectedView() != null ) {
            return fMainWindow.getSelectedView().equals( this );
        } 
        return false;
    }
    
    /**
     * Read  all instances of MModel and maps
     * the specified element to a graphic
     * instance.
     */
    private void initState(boolean isMetamodel) {
    	MModel model = (isMetamodel==false?fSystem.model(): fMetaSystem.model());
        // read Classes
        Collection<MClass> allClasses = model.classes();
        for (MClass cls : allClasses) {
            fClassDiagram.addClass( cls );
        }

        // read Enumerations
        Collection<EnumType> allEnums = model.enumTypes();
        for (EnumType enumeration : allEnums) {
            fClassDiagram.addEnum( enumeration );
        }

        // read signals
        for (MSignal s : model.getSignals()) {
            fClassDiagram.addSignal( s );
        }
        
        // read generalizations
        DirectedGraph<MClassifier, MGeneralization> genGraph = model.generalizationGraph();
        Iterator<MGeneralization> edgeIter = genGraph.edgeIterator();
        while ( edgeIter.hasNext() ) {
            MGeneralization gen = edgeIter.next();
            fClassDiagram.addGeneralization( gen );
        }
 
        // read Associations
        Collection<MAssociation> allAssociations = model.associations();
        for (MAssociation assoc : allAssociations) {
            fClassDiagram.addAssociation( assoc );
        }
        
        fClassDiagram.initialize();
    }
    
    
    @Override
	public void printView( PageFormat pf ) {
        fClassDiagram.printDiagram( pf, "Class diagram" );
    }

    @Override
	public void export( Graphics2D g ) {
    	boolean oldDb = fClassDiagram.isDoubleBuffered();
    	fClassDiagram.setDoubleBuffered(false);
    	fClassDiagram.paint(g);
    	fClassDiagram.setDoubleBuffered(oldDb);
    }

    @Override
	public void detachModel () {}

	@Override
	public float getContentHeight() {
		return fClassDiagram.getPreferredSize().height;
	}
	
	@Override
	public float getContentWidth() {
		return fClassDiagram.getPreferredSize().width;
	}
}