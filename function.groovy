def unstashParam(String name, String fname = null) {
    def paramsAction = currentBuild.rawBuild.getAction(hudson.model.ParametersAction.class);
    if (paramsAction != null) {
        for (param in paramsAction.getParameters()) {
            if (param.getName().equals(name)) {
                if (! param instanceof hudson.model.FileParameterValue) {
                    error "unstashParam: not a file parameter: ${name}"
                }
                if (env['NODE_NAME'] == null) {
                    error "unstashParam: no node in current context"
                }
                if (env['WORKSPACE'] == null) {
                    error "unstashParam: no workspace in current context"
                }

                if (env['NODE_NAME'].equals("master")) {
                    workspace = new hudson.FilePath(null, env['WORKSPACE'])
                } else {
                    // This allows to always unstash the file no matter what is the workspace dir
                    workspace = new hudson.FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), pwd())
                }

                filename = fname == null ? param.getOriginalFileName() : fname
                    file = workspace.child(filename)
                    file.copyFrom(param.getFile())
                    return filename;
            }
        }
    }
    error "unstashParam: No file parameter named '${name}'"
}

// usage:
stage('test') {
    node('test') {
        // checkout scm
        def file_in_workspace = null;
        try {
            file_in_workspace = unstashParam "somefile"
                echo "Found paramter file: ${file_in_workspace}"
        } catch (e) {
            echo "Parameter file not found"
        }
        sh "cat ${file_in_workspace}"
    }
}

