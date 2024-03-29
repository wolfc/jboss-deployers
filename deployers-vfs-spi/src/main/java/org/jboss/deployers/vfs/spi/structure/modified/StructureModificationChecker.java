/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.deployers.vfs.spi.structure.modified;

import java.io.IOException;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.virtual.VirtualFile;

/**
 * Simple structure modification checker interface.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface StructureModificationChecker extends StructureListener
{
   /**
    * Has structure been modified.
    *
    * @param root the structure's root
    * @return true if structure has been modified, false otherwise
    * @throws IOException for any error
    */
   boolean hasStructureBeenModified(VirtualFile root) throws IOException;

   /**
    * Has structure been modified.
    *
    * @param deployment the vfs deployment
    * @return true if structure has been modified, false otherwise
    * @throws IOException for any error
    */
   boolean hasStructureBeenModified(VFSDeployment deployment) throws IOException;

   /**
    * Has structure been modified.
    *
    * @param deploymentContext the vfs deployment context
    * @return true if structure has been modified, false otherwise
    * @throws IOException for any error
    */
   boolean hasStructureBeenModified(VFSDeploymentContext deploymentContext) throws IOException;
}