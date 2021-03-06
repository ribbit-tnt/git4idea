package git4idea.actions;
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * Copyright 2007 Decentrix Inc
 * Copyright 2007 Aspiro AS
 * Copyright 2008 MQSoftware
 * Authors: gevession, Erlend Simonsen & Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */
import git4idea.GitVcs;
import git4idea.GitUtil;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressManager;

import java.util.List;
import java.util.Set;

/**
 * Git "pull" action
 */
public class Pull extends BasicAction {
    @Override
    protected void perform(@NotNull Project project, GitVcs vcs, @NotNull List<VcsException> exceptions,
                           @NotNull VirtualFile[] affectedFiles) throws VcsException {
        saveAll();

        final Set<VirtualFile> roots = GitUtil.getVcsRootsForFiles(project,affectedFiles);
         if (roots.size() == 0) {
            VirtualFile[] proots = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(vcs);
            for (VirtualFile root : proots) {
                if (root != null)
                    roots.add(root);
            }
        }
        for (VirtualFile root : roots) {
            String initialValue = null;
            String repo = Messages.showInputDialog(project,
                    "Enter repository & refspec to pull/merge (empty for default/origin):",
                    "Pull <repository> <refspec>... --> " + root.getPath(), Messages.getQuestionIcon(), initialValue, null);

            GitCommandRunnable cmdr = new GitCommandRunnable(project, vcs.getSettings(), root);
            cmdr.setCommand(GitCommand.PULL_CMD);
            if(repo != null)
                cmdr.setArgs(new String[] { repo });

            ProgressManager manager = ProgressManager.getInstance();
            manager.runProcessWithProgressSynchronously(cmdr, "Pulling... ", false, project);

            VcsException ex = cmdr.getException();
            if(ex != null)  {
                Messages.showErrorDialog(project, ex.getMessage(), "Error occurred during 'git pull'");
                return;
            }
        }        
    }

    @Override
    @NotNull
    protected String getActionName(@NotNull AbstractVcs abstractvcs) {
        return "Pull";
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull GitVcs vcs, @NotNull VirtualFile... vFiles) {
        return true;
    }
}