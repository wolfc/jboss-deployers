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
package org.jboss.test.deployers.vfs.deployer.merge.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.deployers.vfs.deployer.kernel.BeanMetaDataDeployer;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.test.deployers.vfs.deployer.AbstractDeployerUnitTest;
import org.jboss.test.deployers.vfs.deployer.merge.support.TestBeanMergeDeployer;
import org.jboss.test.deployers.support.TCCLClassLoaderDeployer;

/**
 * BeanMergeUnitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class BeanMergeUnitTestCase extends AbstractDeployerUnitTest
{
   public static Test suite()
   {
      return new TestSuite(BeanMergeUnitTestCase.class);
   }

   public BeanMergeUnitTestCase(String name) throws Throwable
   {
      super(name);
   }

   protected void addDeployers(Kernel kernel)
   {
      TestBeanMergeDeployer beanDeployer = new TestBeanMergeDeployer();
      KernelDeploymentDeployer kernelDeploymentDeployer = new KernelDeploymentDeployer();
      BeanMetaDataDeployer beanMetaDataDeployer = new BeanMetaDataDeployer(kernel);
      addDeployer(main, new TCCLClassLoaderDeployer());
      addDeployer(main, beanDeployer);
      addDeployer(main, kernelDeploymentDeployer);
      addDeployer(main, beanMetaDataDeployer);
   }

   public void testMultipleMatchingFiles() throws Exception
   {
      VFSDeployment context = createDeployment("/bean", "multiple/test.jar");
      assertDeploy(context);
      assertNotNull(controller.getInstalledContext("Test1"));
      assertNotNull(controller.getInstalledContext("Test2"));

      assertUndeploy(context);
      assertNull(controller.getContext("Test2", null));
      assertNull(controller.getContext("Test1", null));
   }
}
