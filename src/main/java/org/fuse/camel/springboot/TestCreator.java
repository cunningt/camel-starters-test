package org.fuse.camel.springboot;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

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

    public static void main( String[] args )
    {
        // Assuming that we are taking in a version of 
        String version = args[0];
        String gav = getGAV(version);
        
        TestCreator tc = new TestCreator();
        try {
            Model model = tc.resolveModel(gav);
            System.out.println(model.toString());
            List<Dependency> dependencies = model.getDependencyManagement().getDependencies();
            for (Dependency d : dependencies) {
                System.out.println(d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println (gav);
    }
}
