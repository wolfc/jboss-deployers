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
package org.jboss.test.deployers.deployer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.deployers.deployer.helpers.test.ExactAttachmentDeployerWithVisitorTestCase;
import org.jboss.test.deployers.deployer.test.*;

/**
 * Deployers Deployer Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 37459 $
 */
public class DeployersDeployerTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Deployers Deployer Tests");

      suite.addTest(DeployerProtocolUnitTestCase.suite());
      suite.addTest(DeployerOrderingUnitTestCase.suite());
      suite.addTest(DeployerWidthFirstUnitTestCase.suite());
      suite.addTest(DeployerClassLoaderUnitTestCase.suite());
      suite.addTest(DeployersImplUnitTestCase.suite());
      suite.addTest(ComponentUnitTestCase.suite());
      suite.addTest(MultipleComponentTypeUnitTestCase.suite());
      suite.addTest(HeuristicAllOrNothingUnitTestCase.suite());
      suite.addTest(HeuristicRussionDollUnitTestCase.suite());
      suite.addTest(DeployerContextClassLoaderUnitTestCase.suite());
      suite.addTest(DeployerRequiredStageUnitTestCase.suite());
      suite.addTest(DeployerRequiredInputsUnitTestCase.suite());
      suite.addTest(DynamicRelativeOrderUnitTestCase.suite());

      // sorting tests
      suite.addTest(DeployerFlowUnitTestCase.suite());
      suite.addTest(DominoOrderingUnitTestCase.suite());
      suite.addTest(KahnOrderingUnitTestCase.suite());
      suite.addTest(InOutTopologicalOrderingUnitTestCase.suite());
      suite.addTest(IndexingOrderingUnitTestCase.suite());
      suite.addTest(DependenciesTopologicalOrderingUnitTestCase.suite());

      // helper deployers
      suite.addTest(ExactAttachmentDeployerWithVisitorTestCase.suite());

      return suite;
   }
}
