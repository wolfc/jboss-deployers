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
package org.jboss.test.deployers.vfs.classloader;

import org.jboss.test.deployers.vfs.classloader.test.BootstrapDeployersSmokeTestUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.DeploymentDependsOnDeploymentClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.DeploymentDependsOnManualClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.InMemoryClasesUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.ManagedObjectClassLoadingParserUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.ManifestClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.ManualDependsOnDeploymentClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.NotVFSClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.SubDeploymentClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.SubDeploymentClassLoaderVisitorUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.VFSClassLoaderDependenciesUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.VFSUndeployOrderClassLoaderUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.IntegrationDeployerUnitTestCase;
import org.jboss.test.deployers.vfs.classloader.test.RequirementsIntegrationUnitTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * BeanDeployerTestSuite.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class ClassLoaderTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("VFS ClassLoader Tests");

      suite.addTest(BootstrapDeployersSmokeTestUnitTestCase.suite());
      suite.addTest(VFSClassLoaderDependenciesUnitTestCase.suite());
      suite.addTest(VFSUndeployOrderClassLoaderUnitTestCase.suite());
      suite.addTest(InMemoryClasesUnitTestCase.suite());
      suite.addTest(DeploymentDependsOnManualClassLoaderUnitTestCase.suite());
      suite.addTest(ManualDependsOnDeploymentClassLoaderUnitTestCase.suite());
      suite.addTest(DeploymentDependsOnDeploymentClassLoaderUnitTestCase.suite());
      suite.addTest(ManagedObjectClassLoadingParserUnitTestCase.suite());
      suite.addTest(SubDeploymentClassLoaderUnitTestCase.suite());
      suite.addTest(SubDeploymentClassLoaderVisitorUnitTestCase.suite());
      suite.addTest(NotVFSClassLoaderUnitTestCase.suite());
      suite.addTest(ManifestClassLoaderUnitTestCase.suite());
      suite.addTest(IntegrationDeployerUnitTestCase.suite());
      suite.addTest(RequirementsIntegrationUnitTestCase.suite());

      return suite;
   }
}
