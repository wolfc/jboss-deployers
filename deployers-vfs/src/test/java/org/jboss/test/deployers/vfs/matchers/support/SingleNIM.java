/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.deployers.vfs.matchers.support;

import org.jboss.deployers.spi.deployer.matchers.NameIgnoreMechanism;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SingleNIM implements NameIgnoreMechanism
{
   private String ignore;

   public SingleNIM(String ignore)
   {
      this.ignore = ignore;
   }

   public boolean ignoreName(DeploymentUnit unit, String name)
   {
      return name.equals(ignore);
   }

   public boolean ignorePath(DeploymentUnit unit, String path)
   {
      return path.equals("META-INF/" + ignore);
   }
}
