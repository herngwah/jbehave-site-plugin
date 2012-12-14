package org.jbehave.site;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal jbehave-report
 * 
 * @phase site
 */
public class JBehaveMojo extends AbstractMavenReport {

	/**
	 * Directory where reports will go.
	 * 
	 * @parameter expression="${project.reporting.outputDirectory}/jbehave/"
	 * @required
	 * @readonly
	 */
	private String outputDirectory;

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Directory where jbehave data located.
	 * 
	 * @parameter expression="${project.build.directory}/jbehave"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * JBehave main page (jbehave/index, jbehave/reports, jbehave/maps)
	 * 
	 * @parameter default-value="jbehave/reports"
	 * @required
	 */
	private String output;

	/**
	 * JBehave report formats (html, xml, txt)
	 * 
	 * @parameter
	 */
	private List<String> formats;

	/**
	 * JBehave view resources
	 * 
	 * @parameter
	 */
	private Properties viewResources;

	/**
	 * Force create link
	 * 
	 * @parameter default-value="false"
	 */
	private boolean force;

	/**
	 * @component
	 * @required
	 * @readonly
	 */
	private Renderer siteRenderer;

	private boolean external = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
	 */
	@Override
	public boolean canGenerateReport() {
		if (force || sourceDirectory.exists()) {
			return true;
		} else {
			getLog().info("No story to report.");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
	 */
	@Override
	public String getDescription(Locale arg0) {
		return "JBehave stories report";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
	 */
	@Override
	public String getName(Locale arg0) {
		return "JBehave";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.MavenReport#getOutputName()
	 */
	@Override
	public String getOutputName() {
		return output;
	}

	/**
	 * Get formats
	 * 
	 * @return the formats
	 */
	public List<String> getFormats() {
		if (formats == null) {
			formats = new ArrayList<String>();
			formats.add("html");
		}
		return formats;
	}

	/**
	 * Get view resources
	 * 
	 * @return the viewResources
	 */
	public Properties getViewResources() {
		if (viewResources == null) {
			viewResources = new Properties();
			viewResources.put("viewDirectory", "../site/jbehave");
		}
		return viewResources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#isExternalReport()
	 */
	@Override
	public boolean isExternalReport() {
		return external;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util
	 * .Locale)
	 */
	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		Log log = getLog();
		if (sourceDirectory.exists()) {
			FreemarkerViewGenerator view = new FreemarkerViewGenerator();
			view.generateReportsView(sourceDirectory, getFormats(),
					getViewResources());
			ReportsCount count = view.getReportsCount();
			log.info("Generated " + count.getStories() + " stories with "
					+ count.getScenarios() + " scenarios");
			log.info("Stories: "
					+ (count.getStories() - count.getStoriesNotAllowed() - count
							.getStoriesPending()) + " success "
					+ count.getStoriesPending() + " pending "
					+ count.getStoriesNotAllowed() + " not allowed");
			log.info("Scenarios: "
					+ (count.getScenarios() - count.getScenariosNotAllowed()
							- count.getScenariosPending() + count
								.getScenariosFailed()) + " success "
					+ count.getScenariosPending() + " pending "
					+ count.getScenariosFailed() + " failed "
					+ count.getScenariosNotAllowed() + " not allowed");
			external = true;
			try {
				unpackViewResource();
				copyCss();
			} catch (IOException e) {
				log.warn(e);
			} catch (NoSuchElementException e) {
				log.warn(e);
			} catch (NullPointerException e) {
				log.warn(e);
			}
		} else {
			log.info("No story to report.");
			Sink sink = getSink();
			sink.body();
			sink.section1();
			sink.sectionTitle1();
			sink.text("No story to report.");
			sink.sectionTitle1_();
			sink.section1_();
			sink.body_();
			sink.flush();
			sink.close();
			external = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
	 */
	@Override
	protected String getOutputDirectory() {
		return outputDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
	 */
	@Override
	protected MavenProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
	 */
	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	/**
	 * Unpack view resources
	 * 
	 * @throws IOException
	 *             Io exception
	 * @throws ZipException
	 *             Zip exception
	 * 
	 */
	private void unpackViewResource() throws ZipException, IOException {
		@SuppressWarnings("unchecked")
		Set<Artifact> artifacts = getProject().getArtifacts();
		for (Artifact artifact : artifacts) {
			if (artifact.getArtifactId().equals("jbehave-site-resources")) {
				ZipFile file = new ZipFile(artifact.getFile());
				Enumeration<? extends ZipEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.isDirectory()) {
						new File(outputDirectory + entry.getName()).mkdir();
					} else {
						copy(file.getInputStream(entry),
								new BufferedOutputStream(new FileOutputStream(
										outputDirectory + entry.getName())));
					}
				}
				getLog().info(
						"Unpacked " + file.getName() + " to " + outputDirectory);
			}
		}
	}

	/**
	 * Copy file
	 * 
	 * @param in
	 *            Input stream
	 * @param out
	 *            Output stream
	 * @throws IOException
	 *             Io exception
	 */
	private void copy(InputStream in, BufferedOutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Copy style from jar
	 * 
	 * @throws IOException
	 *             Io exception
	 */
	private void copyCss() throws IOException {
		URL css = this.getClass().getResource("/style/jbehave-core.css");
		FileUtils.copyURLToFile(css, new File(outputDirectory
				+ "style/jbehave-core.css"));
	}

}
