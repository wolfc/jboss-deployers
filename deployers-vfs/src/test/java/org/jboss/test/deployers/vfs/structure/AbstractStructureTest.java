/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.deployers.vfs.structure;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.vfs.plugins.structure.VFSStructuralDeployersImpl;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.plugins.structure.file.FileStructure;
import org.jboss.deployers.vfs.plugins.structure.jar.JARStructure;
import org.jboss.deployers.vfs.plugins.structure.war.WARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.StructureDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.test.BaseTestCase;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFSUtils;

/**
 * AbstractStructureUnitTestCase.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStructureTest extends BaseTestCase
{
   public AbstractStructureTest(String name)
   {
      super(name);
   }

   protected void assertUnpacked(VirtualFile file) throws Exception
   {
      VirtualFile modified = VFSUtils.unpack(file);
      assertTrue(VFSUtils.isTemporaryFile(modified));
   }

   protected void assertNoChildContexts(VFSDeploymentContext context)
   {
      assertChildContexts(context);
   }

   protected void assertChildContexts(VFSDeploymentContext context, String... paths)
   {
      assertChildContexts(context, false, paths);
   }

   protected void assertChildContexts(VFSDeploymentContext context, boolean flatten, String... paths)
   {
      List<String> expected = new ArrayList<String>();
      if (paths != null)
      {
         for (String path : paths)
            expected.add(path);
      }

      List<DeploymentContext> children;
      if (flatten)
      {
         children = new ArrayList<DeploymentContext>();
         flattenContexts(children, context);
      }
      else
      {
         children = context.getChildren();
      }
      assertNotNull(children);
      assertEquals("Expected " + expected + " got " + simplePrint(children), expected.size(), children.size());
      
      for (String path : expected)
      {
         boolean found = false;
         for (DeploymentContext child : children)
         {
            String childPath = child.getRelativePath();
            if (path.equals(childPath))
            {
               found = true;
               break;
            }
            if (flatten)
            {
               DeploymentContext parent = child.getParent();
               if (parent != null)
               {
                  String parentPath = parent.getRelativePath();
                  if (parentPath.endsWith("/") == false && childPath.startsWith("/") == false)
                     parentPath += "/";

                  if (path.equals(parentPath + childPath))
                  {
                     found = true;
                     break;
                  }
               }
            }
         }
         if (found == false)
            fail("Expected " + path + " in " + children);
      }
   }

   protected void flattenContexts(List<DeploymentContext> contexts, DeploymentContext context)
   {
      List<DeploymentContext> children = context.getChildren();
      if (children != null)
      {
         for (DeploymentContext dc : children)
         {
            contexts.add(dc);
            flattenContexts(contexts, dc);
         }
      }
   }

   protected String simplePrint(List<DeploymentContext> children)
   {
      StringBuilder builder = new StringBuilder();
      boolean first = false;
      builder.append("[");
      for (DeploymentContext child : children)
      {
         if (first == false)
            first = true;
         else
            builder.append(", ");
         builder.append(child.getRelativePath());
      }
      builder.append("]");
      return builder.toString();
   }
   
   protected void assertMetaData(VFSDeploymentContext context, String metaDataPath) throws Exception
   {
      VirtualFile root = context.getRoot();
      List<VirtualFile> metaDataLocation = context.getMetaDataLocations();
      VirtualFile expected = root.findChild(metaDataPath);
      assertNotNull(metaDataLocation);
      assertEquals(1, metaDataLocation.size());
      assertEquals(expected, metaDataLocation.get(0));
   }
   
   protected void assertMetaDatas(VFSDeploymentContext context, String... metaDataPath) throws Exception
   {
      VirtualFile root = context.getRoot();
      List<VirtualFile> metaDataLocations = context.getMetaDataLocations();
      assertNotNull(metaDataLocations);
      int i = 0;
      for(String path : metaDataPath)
      {
         VirtualFile expected = root.findChild(path);
         assertEquals(expected, metaDataLocations.get(i++));
      }
   }

   protected VFSDeploymentContext assertChildContext(VFSDeploymentContext context, String name) throws Exception
   {
      List<DeploymentContext> children = context.getChildren();
      if (children == null)
         fail("No children for " + context);
      for (DeploymentContext child : children)
      {
         if (name.equals(child.getSimpleName()))
            return (VFSDeploymentContext) child;
      }
      fail(name + " not found in " + children);
      throw new RuntimeException("unreachable");
   }

   protected void assertClassPath(VFSDeploymentContext context, String... paths) throws Exception
   {
      assertClassPath(context, context, paths);
   }
   
   protected void assertClassPath(VFSDeploymentContext context, VFSDeploymentContext reference, String... paths) throws Exception
   {
      List<VirtualFile> classPath = context.getClassPath();
      if (paths == null || paths.length == 0)
      {
         if (classPath != null)
            assertEmpty(classPath);
         return;
      }
      
      assertNotNull("Expected " + Arrays.asList(paths), classPath);
      
      assertEquals("Expected " + Arrays.asList(paths) + " got " + classPath, paths.length, classPath.size());
      
      for (String path : paths)
      {
         VirtualFile ref = reference.getRoot();
         VirtualFile expected = ref.findChild(path);
         assertTrue("Expected " + expected +" in " + classPath, classPath.contains(expected));
      }
   }
   
   protected void assertMetaDataFile(VFSDeploymentContext context, String name)
   {
      assertNotNull("Should find metadata " + name, context.getMetaDataFile(name));
   }
   
   protected void assertNoMetaDataFile(VFSDeploymentContext context, String name)
   {
      assertNull("Should not find metadata " + name, context.getMetaDataFile(name));
   }
   
   protected void assertFile(VFSDeploymentContext context, String name)
   {
      assertNotNull("Should find file " + name, context.getFile(name));
   }
   
   protected void assertNoFile(VFSDeploymentContext context, String name)
   {
      assertNull("Should not find file " + name, context.getFile(name));
   }

   protected abstract VFSDeploymentContext determineStructure(VFSDeployment deployment) throws Exception;
   
   protected VFSDeploymentContext determineStructureWithStructureDeployer(VFSDeployment deployment, StructureDeployer structureDeployer) throws Exception
   {
      return determineStructureWithStructureDeployers(deployment, structureDeployer);
   }
   
   protected VFSDeploymentContext determineStructureWithAllStructureDeployers(VFSDeployment deployment) throws Exception
   {
      return determineStructureWithStructureDeployers(deployment, new FileStructure(), new WARStructure(), new JARStructure());
   }
   
   protected VFSDeploymentContext determineStructureWithStructureDeployers(VFSDeployment deployment, StructureDeployer... deployers) throws Exception
   {
      return determineStructureWithStructureDeployers(deployment, true, deployers);
   }

   protected VFSDeploymentContext determineStructureWithStructureDeployers(VFSDeployment deployment, boolean serialize, StructureDeployer... deployers) throws Exception
   {
      VFSStructuralDeployersImpl structuralDeployers = new VFSStructuralDeployersImpl();
      VFSStructureBuilder builder = new VFSStructureBuilder();
      structuralDeployers.setStructureBuilder(builder);
      
      for (StructureDeployer deployer : deployers)
         structuralDeployers.addDeployer(deployer);

      VFSDeploymentContext context = (VFSDeploymentContext)structuralDeployers.determineStructure(deployment);
      if (serialize)
         return serializeDeserialize(context, VFSDeploymentContext.class);
      else
         return context;
   }
   
   protected VFSDeploymentContext deploy(String context, String path) throws Throwable
   {
      VFSDeployment deployment = createDeployment(context, path);
      VFSDeploymentContext result = determineStructure(deployment);
      assertNotNull(result);
      if (result.getProblem() != null)
         throw result.getProblem();
      return result;
   }
   
   protected VFSDeploymentContext assertDeploy(String context, String path) throws Throwable
   {
      VFSDeploymentContext result = deploy(context, path);
      assertEquals(path, result.getSimpleName());
      return result;
   }
   
   protected VFSDeploymentContext assertDeployNoChildren(String context, String path) throws Throwable
   {
      VFSDeploymentContext result = assertDeploy(context, path);
      assertNoChildContexts(result);
      return result;
   }
   
   protected void assertNotValid(String context, String path) throws Throwable
   {
      try
      {
         deploy(context, path);
         fail("Should not be here!");
      }
      catch (Exception e)
      {
         checkThrowable(DeploymentException.class, e);
      }
   }
   
   protected VFSDeployment createDeployment(String context, String path) throws Exception
   {
      VirtualFile root = getVirtualFile(context, path);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      return serializeDeserialize(deployment, VFSDeployment.class);
   }

   protected VirtualFile getVirtualFile(String root, String path) throws Exception
   {
      URL url = getResource(root);
      return VFS.getVirtualFile(url, path);
   }

   @Override
   public URL getResource(String path)
   {
      URL result = super.getResource(path);
      if (result == null)
         fail("Unable to find resource " + path);
      return result;
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      //enableTrace("org.jboss.deployers");
   }
}
