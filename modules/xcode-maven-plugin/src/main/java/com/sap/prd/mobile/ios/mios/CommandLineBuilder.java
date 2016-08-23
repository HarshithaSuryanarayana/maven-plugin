/*
 * #%L
 * xcode-maven-plugin
 * %%
 * Copyright (C) 2012 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.sap.prd.mobile.ios.mios;
import com.sap.prd.mobile.ios.mios.AbstractXCodeMojo;

import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.checkVersions;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getVersion;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getXCodeVersionString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

class CommandLineBuilder
{

  private final static String XCODEBUILD = "xcodebuild";
  private final static String XCCONFIG = "-xcconfig";
  
  private final static String XCPATH="~/CentralGenericSigning.xcconfig";
  public final static String MINIMUM_VERSION = "8.0";
  protected MavenProject project;
 

  protected String packaging;
  private XCodeContext xcodeContext;

  public CommandLineBuilder(XCodeContext ctx)
  {
    this.xcodeContext = ctx;
  }

  String[] createBuildCall()
  {
	  

    List<String> result = new ArrayList<String>();
    result.add(XCODEBUILD);
    
    appendOptions(xcodeContext, result);
    for (String buildAction : xcodeContext.getBuildActions()) {
      appendValue(result, buildAction);
    }
   
    appendSettings(xcodeContext.getSettings(), result);
    try {
        String xCodeVersionString = getXCodeVersionString();
        DefaultArtifactVersion version = getVersion(xCodeVersionString);
        if(checkVersions(version,MINIMUM_VERSION))
        {
        	//getLog().info("Xcode version is" + version);
        result.add(XCCONFIG);
        final Properties projectProperties = project.getProperties();
        String xcconfigFile = projectProperties.getProperty("$CentralSigning.xcconfig");
        File file =new File(xcconfigFile);
        if(file.exists())
        {
        result.add(XCPATH);
        }
        else
        {
        result.add(XCPATH);
        }
        }
    }
    catch (XCodeException e) {
    	throw new IllegalStateException("Could not get xcodebuild version");
      }
       
   return result.toArray(new String[result.size()]);
  }

  private static void appendOptions(XCodeContext xcodeContext, List<String> result)
  {
    Map<String, String> options = xcodeContext.getOptions().getAllOptions();
    for (Map.Entry<String, String> entry : options.entrySet()) {
      appendOption(result, entry.getKey(), entry.getValue());
    }
  }

  private static void appendOption(List<String> result, String key, String value)
  {

    CommandLineBuilder.appendKey(result, key);
    if (value != null)
      CommandLineBuilder.appendValue(result, value);
  }

  private static void appendSettings(Settings settings, List<String> result)
  {

    for (Map.Entry<String, String> entry : settings.getAllSettings().entrySet()) {
      appendSetting(result, entry.getKey(), entry.getValue());
    }
  }

  private static void appendSetting(List<String> result, String key, String value)
  {
    result.add(key + "=" + value);
  }

  static void appendKey(List<String> result, String key)
  {
    check("key", key);
    result.add("-" + key);
  }

  static void appendValue(List<String> result, String value)
  {
    check("value", value);
    result.add(value);
    
  }

  static void check(final String name, final String forCheck)
  {
    if (forCheck == null || forCheck.isEmpty())
      throw new IllegalStateException("Invalid " + name + ": '" + forCheck + "'. Was null or empty.");
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder(256);
    boolean first = true;
    for (String part : createBuildCall()) {
      if (!first)
        sb.append(" ");
      else
        first = false;
      sb.append(part);
    }
    return sb.toString();
  }
}
