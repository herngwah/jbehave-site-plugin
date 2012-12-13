jbehave-site-plugin
===================

JBehave report for maven report

add to pom.xml

<reporting>
	<plugins>
		<plugin>
			<groupId>org.jbehave.site</groupId>
			<artifactId>jbehave-site-plugin</artifactId>
			<version>0.0.3</version>
			<configuration>
				<formats>
					<format>html</format>
					<format>xml</format>
					<format>txt</format>
				</formats>
			</configuration>
		</plugin>
</reporting>
<dependencies>
	<dependency>
		<groupId>org.jbehave</groupId>
		<artifactId>jbehave-core</artifactId>
		<version>RELEASE</version>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>org.jbehave.site</groupId>
		<artifactId>jbehave-site-resources</artifactId>
		<version>RELEASE</version>
		<type>zip</type>
		<scope>test</scope>
	</dependency>
</dependencies>
