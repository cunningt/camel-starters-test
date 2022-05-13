package org.fuse.camel.springboot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;

public class TestCreator 
{
    private final static String GROUP_ID = "org.apache.camel.springboot";
    private final static String ARTIFACT_ID = "camel-spring-boot"; 


    public static String getGAV(String version) {
        return GROUP_ID + ":" + ARTIFACT_ID + ":" + version;
    }

    public Model resolveModel(String gav) throws Exception {
        System.out.println(gav);
        File[] file = Maven.resolver().resolve(gav).withoutTransitivity().asFile();
        String fileName = file[0].getAbsolutePath();
        fileName = fileName.replaceAll("camel-spring-boot", "camel-spring-boot-bom");
        fileName = fileName.replaceAll(".jar", ".pom");

        System.out.println(fileName);

        PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile(fileName);
        ParsedPomFile parsedPom = ((MavenWorkingSessionContainer) resolver).getMavenWorkingSession().getParsedPomFile();

        Model model = parsedPom.getModel();
        return model;
    }

    public Model readModel(String pomFileName) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pomFileName));
        return model;
    }

    public void writeDependencies(Model model, List<Dependency> dependencies, String version) throws Exception {
        for (Dependency d : dependencies) {
            model.addDependency(d);
        }
        model.setProperty("camel-spring-boot-version", version);
    }

    public void writeModel(Model model, String pomFileName) throws Exception {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileWriter(pomFileName), model);
    }

    public static void main(String[] args)
    {
        // Assuming that we are taking in a version of 
        String version = args[0];
        String templateFile = args[1];
        String outputFile = args[2];

        String gav = getGAV(version);
        
        TestCreator tc = new TestCreator();
        try {
            Model model = tc.resolveModel(gav);

            List<Dependency> dependencies = model.getDependencyManagement().getDependencies();
            for (Dependency d : dependencies) {
                System.out.println(d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion());
            }

            Model templateModel = tc.readModel(templateFile);
            tc.writeDependencies(templateModel, dependencies, version);
            tc.writeModel(templateModel, outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println (gav);
    }
}
