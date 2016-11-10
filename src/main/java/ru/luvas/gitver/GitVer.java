package ru.luvas.gitver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.File;
import java.util.Date;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Mojo(name = "describe", defaultPhase = LifecyclePhase.INITIALIZE)
public class GitVer extends AbstractMojo {

    @Parameter(property = "project", readonly = true)
    private MavenProject project;
    
    @Parameter(defaultValue = "gitcommitid")
    private String gitCommitIdProperty;
    
    @Parameter(defaultValue = "gitcommitdate")
    private String gitCommitDateProperty;
    
    @Parameter(defaultValue = "false")
    private boolean override;
    
    @Parameter(property = "maven.changeSet.scmDirectory", defaultValue = "${project.basedir}")
    private File scmDirectory;
    
    @Parameter(defaultValue = "unknown")
    private String failHash;
    
    @Parameter(defaultValue = "40")
    private int hashLength;

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void execute() throws MojoExecutionException {
        try(Repository repository = new FileRepositoryBuilder().findGitDir(scmDirectory).build()) {
            findHash(repository);
            findDate(repository);
        }catch(RepositoryNotFoundException ex) {
            warn("Could not find Git repository!");
        }catch(Exception ex) {
            warn("There is unexpected exception during GitVer execution: %s:%s", ex.getClass().getName(), ex.getMessage());
        }
    }
    
    private void findHash(Repository repository) throws Exception {
        if(!override && project.getProperties().containsKey(gitCommitIdProperty)) {
            log("Can not setup property '%s', because it already exists.", gitCommitIdProperty);
            return;
        }
        ObjectId head = repository.resolve(Constants.HEAD);
        if(head == null) {
            warn("There are no commits on the repository!");
            return;
        }
        try(ObjectReader reader = repository.newObjectReader()) {
            String hash = reader.abbreviate(head, hashLength).name();
            if(hash == null)
                hash = failHash;
            project.getProperties().put(gitCommitIdProperty, hash);
            log("Set property '%s' to '%s'", gitCommitIdProperty, hash);
        }
    }
    
    private void findDate(Repository repository) throws Exception {
        if(!override && project.getProperties().containsKey(gitCommitDateProperty)) {
            log("Can not setup property '%s', because it already exists.", gitCommitDateProperty);
            return;
        }
        try(RevWalk walk = new RevWalk(repository)) {
            ObjectId head = repository.resolve(Constants.HEAD);
            if(head == null) {
                warn("There are no commits on the repository!");
                return;
            }
            walk.markStart(walk.parseCommit(head));
            RevCommit commit = walk.next();
            Date date = new Date(commit.getCommitTime() * 1000l);
            @SuppressWarnings("deprecation")
            String sdate = date.toGMTString();
            project.getProperties().put(gitCommitDateProperty, sdate);
            log("Set property '%s' to '%s'", gitCommitDateProperty, sdate);
        }
    }
    
    private void log(Object s) {
        getLog().info(s.toString());
    }
    
    private void log(String s, Object... args) {
        log(String.format(s, args));
    }
    
    private void warn(Object s) {
        getLog().warn("Warning: " + s);
    }
    
    private void warn(String s, Object... args) {
        warn(String.format(s, args));
    }
    
}
