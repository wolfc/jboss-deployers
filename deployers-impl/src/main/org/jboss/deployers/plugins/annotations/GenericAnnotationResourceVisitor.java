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
package org.jboss.deployers.plugins.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMember;
import javassist.NotFoundException;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.jboss.classloading.spi.visitor.ClassFilter;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.signature.Signature;
import org.jboss.metadata.spi.signature.javassist.JavassistSignatureFactory;
import org.jboss.metadata.spi.signature.javassist.JavassistConstructorParametersSignature;
import org.jboss.metadata.spi.signature.javassist.JavassistMethodParametersSignature;

/**
 * Generic annotation scanner deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class GenericAnnotationResourceVisitor implements ResourceVisitor
{
   private static final Logger log = Logger.getLogger(GenericAnnotationResourceVisitor.class);

   private ClassPool pool;
   private boolean forceAnnotations;
   private DefaultAnnotationEnvironment env;
   private CtClass objectCtClass;

   public GenericAnnotationResourceVisitor(ClassLoader classLoader)
   {
      this(ClassPool.getDefault(), classLoader);
   }

   public GenericAnnotationResourceVisitor(ClassPool pool, ClassLoader classLoader)
   {
      if (pool == null)
         throw new IllegalArgumentException("Null pool");
      if (classLoader == null)
         throw new IllegalArgumentException("Null classloader");

      this.pool = pool;
      this.env = new DefaultAnnotationEnvironment(classLoader);
      this.objectCtClass = pool.makeClass(Object.class.getName());
   }

   public ResourceFilter getFilter()
   {
      return ClassFilter.INSTANCE;
   }

   public void visit(ResourceContext resource)
   {
      try
      {
         CtClass ctClass = pool.makeClass(resource.getInputStream());
         try
         {
            handleCtClass(ctClass, resource);
         }
         finally
         {
            ctClass.detach();               
         }
      }
      catch (ClassNotFoundException e)
      {
         if (forceAnnotations)
            throw new RuntimeException(e);

         logThrowable(resource, e);
      }
      catch (Throwable t)
      {
         logThrowable(resource, t);
      }
   }

   /**
    * Log throwable.
    *
    * @param resource the resource we're visiting
    * @param t the throwable
    */
   protected void logThrowable(ResourceContext resource, Throwable t)
   {
      if (log.isTraceEnabled())
         log.trace("Exception reading resource: " + resource.getResourceName(), t);
   }

   /**
    * Handle CtClass for annotations.
    *
    * @param ctClass the ct class instance
    * @param resource the resource we're visiting
    * @throws ClassNotFoundException for any annotations lookup problems
    * @throws NotFoundException for any annotations lookup problems
    */
   protected void handleCtClass(CtClass ctClass, ResourceContext resource) throws ClassNotFoundException, NotFoundException
   {
      if (ctClass == null || objectCtClass.equals(ctClass))
         return;

      if (log.isTraceEnabled())
         log.trace("Scanning class " + ctClass + " for annotations, resource url: " + resource.getUrl());

      Object[] annotations = forceAnnotations ? ctClass.getAnnotations() : ctClass.getAvailableAnnotations();
      handleAnnotations(ElementType.TYPE, (Signature)null, annotations, resource);
      handleCtMembers(ElementType.CONSTRUCTOR, ctClass.getDeclaredConstructors(), resource);
      handleCtMembers(ElementType.METHOD, ctClass.getDeclaredMethods(), resource);
      handleCtMembers(ElementType.FIELD, ctClass.getDeclaredFields(), resource);

      // interfaces
      CtClass[] interfaces = ctClass.getInterfaces();
      if (interfaces != null && interfaces.length > 0)
      {
         for (CtClass intf : interfaces)
            handleCtClass(intf, resource);
      }

      // super class
      handleCtClass(ctClass.getSuperclass(), resource);
   }

   /**
    * Handle CtMembers for annotations.
    *
    * @param type where we found the annotations
    * @param members the ct member instances
    * @param resource the resource we're visiting
    * @throws ClassNotFoundException for any annotations lookup problems
    */
   protected void handleCtMembers(ElementType type, CtMember[] members, ResourceContext resource) throws ClassNotFoundException
   {
      if (members != null && members.length > 0)
      {
         for (CtMember member : members)
         {
            Object[] annotations = forceAnnotations ? member.getAnnotations() : member.getAvailableAnnotations();
            handleAnnotations(type, member, annotations, resource);
            if (member instanceof CtBehavior)
            {
               CtBehavior behavior = (CtBehavior)member;
               Object[][] paramAnnotations = forceAnnotations ? behavior.getParameterAnnotations() : behavior.getAvailableParameterAnnotations();
               for (int index = 0; index < paramAnnotations.length; index++)
               {
                  handleAnnotations(ElementType.PARAMETER, getBehaviorSignature(behavior, index), paramAnnotations[index], resource);                  
               }
            }
         }
      }
   }

   /**
    * Get parameters signature.
    *
    * @param behavior the ct behavior
    * @param index the index
    * @return parameters signature
    * @throws ClassNotFoundException for any error
    */
   protected Signature getBehaviorSignature(CtBehavior behavior, int index) throws ClassNotFoundException
   {
      try
      {
         if (behavior instanceof CtConstructor)
            return new JavassistConstructorParametersSignature((CtConstructor)behavior, index);
         else if (behavior instanceof CtMethod)
            return new JavassistMethodParametersSignature((CtMethod)behavior, index);
         else
            throw new IllegalArgumentException("Unknown ct behavior: " + behavior);
      }
      catch (NotFoundException e)
      {
         throw new ClassNotFoundException("Exception creating signature: " + behavior, e);
      }
   }

   /**
    * Handle annotations.
    *
    * @param type where we found the annotations
    * @param member the ct member
    * @param annotations the actual annotations
    * @param resource the resource we're visiting
    */
   protected void handleAnnotations(ElementType type, CtMember member, Object[] annotations, ResourceContext resource)
   {
      Signature signature = null;
      if (member != null)
         signature = JavassistSignatureFactory.getSignature(member);
      handleAnnotations(type, signature, annotations, resource);
   }

   /**
    * Handle annotations.
    *
    * @param type where we found the annotations
    * @param signature the signature
    * @param annotations the actual annotations
    * @param resource the resource we're visiting
    */
   protected void handleAnnotations(ElementType type, Signature signature, Object[] annotations, ResourceContext resource)
   {
      if (annotations != null && annotations.length > 0)
      {
         for (Object annObject : annotations)
         {
            Annotation annotation = Annotation.class.cast(annObject);
            env.putAnnotation(annotation.annotationType(), type, resource.getClassName(), signature);
         }
      }
   }

   /**
    * Should we force all annotations to be available.
    *
    * @param forceAnnotations the force annotations flag
    */
   public void setForceAnnotations(boolean forceAnnotations)
   {
      this.forceAnnotations = forceAnnotations;
   }

   /**
    * Get the built environment.
    *
    * @return the annoattion environment
    */
   public AnnotationEnvironment getEnv()
   {
      return env;
   }
}