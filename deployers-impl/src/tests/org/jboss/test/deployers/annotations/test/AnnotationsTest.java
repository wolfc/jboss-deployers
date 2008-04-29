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
package org.jboss.test.deployers.annotations.test;

import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.dependency.policy.mock.MockClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.CapabilitiesMetaData;
import org.jboss.classloading.spi.metadata.Capability;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.plugins.classloading.AbstractClassLoaderDescribeDeployer;
import org.jboss.deployers.plugins.annotations.GenericAnnotationDeployer;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments;
import org.jboss.deployers.spi.deployer.Deployer;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.test.deployers.AbstractDeployerTest;
import org.jboss.test.deployers.classloading.support.MockClassLoaderDescribeDeployer;
import org.jboss.test.deployers.classloading.support.MockLevelClassLoaderSystemDeployer;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AnnotationsTest extends AbstractDeployerTest
{
   private static ClassLoadingMetaDataFactory classLoadingMetaDataFactory = ClassLoadingMetaDataFactory.getInstance();

   protected AbstractClassLoaderDescribeDeployer deployer1;
   protected MockLevelClassLoaderSystemDeployer deployer2;

   protected AnnotationsTest(String name)
   {
      super(name);
   }

   protected static ClassLoadingMetaData addClassLoadingMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, Class<?>... packages)
   {
      return addClassLoadingMetaData(deployment, name, version, false, packages);
   }

   protected static ClassLoadingMetaData addClassLoadingMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      ClassLoadingMetaData classLoadingMetaData = createMetaData(deployment, name, version, useVersionOnPackages, packages);
      addMetaData(deployment, classLoadingMetaData);
      return classLoadingMetaData;
   }

   protected static ClassLoadingMetaData createMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, Class<?>... packages)
   {
      return createMetaData(deployment, name, version, false, packages);
   }

   protected static ClassLoadingMetaData createMetaData(PredeterminedManagedObjectAttachments deployment, String name, Version version, boolean useVersionOnPackages, Class<?>... packages)
   {
      MockClassLoadingMetaData classLoadingMetaData = new MockClassLoadingMetaData(name, version);

      classLoadingMetaData.setPaths(packages);

      CapabilitiesMetaData capabilities = classLoadingMetaData.getCapabilities();
      Capability capability = classLoadingMetaDataFactory.createModule(name, version);
      capabilities.addCapability(capability);

      if (packages != null)
      {
         for (Class<?> pkg : packages)
         {
            if (useVersionOnPackages)
               capability = classLoadingMetaDataFactory.createPackage(pkg.getPackage().getName(), version);
            else
               capability = classLoadingMetaDataFactory.createPackage(pkg.getPackage().getName());
            capabilities.addCapability(capability);
         }
      }

      classLoadingMetaData.setCapabilities(capabilities);
      return classLoadingMetaData;
   }

   protected static void addMetaData(PredeterminedManagedObjectAttachments attachments, ClassLoadingMetaData md)
   {
      MutableAttachments mutable = (MutableAttachments) attachments.getPredeterminedManagedObjects();
      mutable.addAttachment(ClassLoadingMetaData.class, md);
   }

   protected AnnotationEnvironment getAnnotationEnvironment(PredeterminedManagedObjectAttachments attachments)
   {
      AnnotationEnvironment env = attachments.getPredeterminedManagedObjects().getAttachment(AnnotationEnvironment.class);
      assertNotNull(env);
      return env;
   }

   protected DeployerClient getMainDeployer(Deployer... deployers)
   {
      ClassLoading classLoading = new ClassLoading();
      ClassLoaderSystem system = new DefaultClassLoaderSystem();
      system.getDefaultDomain().setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);

      deployer1 = new MockClassLoaderDescribeDeployer();
      deployer1.setClassLoading(classLoading);

      deployer2 = new MockLevelClassLoaderSystemDeployer();
      deployer2.setClassLoading(classLoading);
      deployer2.setSystem(system);

      Deployer deployer3 = new GenericAnnotationDeployer();

      if (deployers != null && deployers.length > 0)
      {
         Deployer[] allDeployers = new Deployer[deployers.length + 3];
         allDeployers[0] = deployer1;
         allDeployers[1] = deployer2;
         allDeployers[2] = deployer3;
         System.arraycopy(deployers, 0, allDeployers, 3, deployers.length);
         return createMainDeployer(allDeployers);
      }

      return createMainDeployer(deployer1, deployer2, deployer3);
   }
}