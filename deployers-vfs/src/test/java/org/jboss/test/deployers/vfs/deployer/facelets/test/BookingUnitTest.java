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
package org.jboss.test.deployers.vfs.deployer.facelets.test;

import java.net.URL;
import java.util.Set;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.plugins.classloading.AbstractLevelClassLoaderSystemDeployer;
import org.jboss.deployers.plugins.classloading.ClassLoadingDefaultDeployer;
import org.jboss.deployers.structure.spi.StructureBuilder;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderClassPathDeployer;
import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderDescribeDeployer;
import org.jboss.deployers.vfs.plugins.structure.VFSStructureBuilder;
import org.jboss.deployers.vfs.plugins.structure.modify.ModificationTypeStructureProcessor;
import org.jboss.deployers.vfs.plugins.structure.modify.TempTopModificationTypeMatcher;
import org.jboss.deployers.vfs.plugins.structure.war.WARStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.facelets.support.SearchDeployer;
import org.jboss.test.deployers.vfs.structure.ear.support.MockEarStructureDeployer;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.plugins.cache.IterableTimedVFSCache;
import org.jboss.virtual.spi.cache.VFSCache;
import org.jboss.virtual.spi.cache.VFSCacheFactory;

/**
 * BookingUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BookingUnitTest extends AbstractDeployerUnitTest
{
   private SearchDeployer deployer = new SearchDeployer("META-INF/", ".taglib.xml");

   public BookingUnitTest(String name) throws Throwable
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      // Uncomment this to test VFS nested jar copy handling
      System.setProperty(VFSUtils.FORCE_COPY_KEY, "true");

      VFSCache cache = new IterableTimedVFSCache();
      cache.start();
      VFSCacheFactory.setInstance(cache);

      addStructureDeployer(main, new WARStructure());
      addStructureDeployer(main, new MockEarStructureDeployer());
   }

   @Override
   protected void tearDown() throws Exception
   {
      VFSCacheFactory.setInstance(null);
      super.tearDown();
   }

   protected void addDeployers(Kernel kernel)
   {
      ClassLoadingDefaultDeployer cldd = new ClassLoadingDefaultDeployer();
      ClassLoadingMetaData clmd = new ClassLoadingMetaData();
      cldd.setDefaultMetaData(clmd);

      VFSClassLoaderClassPathDeployer vfscp = new VFSClassLoaderClassPathDeployer();
      VFSClassLoaderDescribeDeployer vfsdd = new VFSClassLoaderDescribeDeployer();
      ClassLoading classLoading = new ClassLoading();
      KernelController controller = kernel.getController();
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("ClassLoading", ClassLoading.class.getName());
      builder.addMethodInstallCallback("addModule");
      builder.addMethodUninstallCallback("removeModule");
      try
      {
         controller.install(builder.getBeanMetaData(), classLoading);
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
      vfsdd.setClassLoading(classLoading);

      ClassLoaderSystem system = new DefaultClassLoaderSystem();
      system.getDefaultDomain().setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);

      AbstractLevelClassLoaderSystemDeployer clsd = new AbstractLevelClassLoaderSystemDeployer();
      clsd.setClassLoading(classLoading);
      clsd.setSystem(system);

      addDeployer(main, cldd);
      addDeployer(main, vfsdd);
      addDeployer(main, vfscp);
      addDeployer(main, clsd);
      addDeployer(main, deployer);
   }

   @Override
   protected StructureBuilder createStructureBuilder()
   {
      VFSStructureBuilder structureBuilder = (VFSStructureBuilder)super.createStructureBuilder();
      ModificationTypeStructureProcessor sp = new ModificationTypeStructureProcessor();
      sp.addMatcher(new TempTopModificationTypeMatcher("META-INF/components.xml"));
      structureBuilder.setStructureProcessor(sp);
      return structureBuilder;
   }

   protected void testFacelets(String name, int size) throws Throwable
   {
      VFSDeployment context = createDeployment("/facelets", name);
      assertDeploy(context);
      try
      {
         Set<URL> urls = deployer.getUrls();
         assertNotNull(urls);
         assertEquals(size, urls.size());
      }
      finally
      {
         assertUndeploy(context);
      }
   }

   public void testBooking() throws Throwable
   {
      testFacelets("booking.ear", 11);
   }
}