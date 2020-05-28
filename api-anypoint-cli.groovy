pipeline {
    agent any
	   environment {
	   anypoint_credentials = credentials('anypoint')
	   VALUE_ONE = 'Create'
       VALUE_TWO = 'Update'
	   VALUE_THREE = 'Promote'
	   APP_REPO='https://github.com/Sateesh-tek/api-manager.git'
	   APP_BRANCH = 'master'
	   USR = "${anypoint_credentials_usr}"
	   PSW = "${anypoint_credentials_psw}"
	   
    }
     parameters {
		choice(name: 'Business_Group', choices: ['TEKsystems'], description: '')
		choice(name: 'Actions', choices: ['Create', 'Update', 'Promote'], description: '')
		choice(name: 'Target_Environment', choices: ['Sandbox', 'QA', 'QA1', 'QA2', 'QA3', 'QA1', 'Staging'], description: '')
		choice(name: 'Source_Environment', choices: ['Sandbox', 'QA', 'QA1', 'QA2', 'QA3', 'QA1', 'Staging'], description: 'Only Required During Promote Action')
		string(name: 'API_Asset_Id', defaultValue: '', description: 'Example: System-Walmart-WOMS. Only Required During Create Action')
		string(name: 'API_Asset_Version', defaultValue: '', description: 'Example: 1.0.2. Refer Exchange For Asset Version')
	    string(name: 'API_InstanceId', defaultValue: '', description: 'Only Required During Update or Promote Action. Example: 15940311. Refer API Administration')
    }
   stages {
       stage('Clone') {
            steps {
                //Note since we are working with additional files beyond what is in this repo we should clone into separate directory.
                //Also see https://issues.jenkins-ci.org/browse/JENKINS-22795
                sh 'rm -rf *'
                
                dir ('code') {
                    echo "\u2776 Checkout the ${APP_BRANCH} branch of ${APP_REPO}"
                   
                    //Use the full checkout directive in order to caputre git info
                    script {
                        git_info = checkout(
                            [
                                $class: 'GitSCM',
                                poll: true,
                                branches: [[name: "*/${APP_BRANCH}"]],
                                doGenerateSubmoduleConfigurations: false,
                                extensions: [[$class: 'AuthorInChangelog']],
                                submoduleCfg: [],
                                userRemoteConfigs: [
                                    [
                                        credentialsId: 'github',
                                        url: "${APP_REPO}"
                                    ]
                                ]
                            ]
                        )
                    }
                }
                sh 'chmod -R 777 code/api.sh'
            }
        }
   stage ('Prepare Environment') 
		{
		   when {
            expression {
               params.Actions == 'Promote'
               }
            }
	        steps 
			{
			    script 
				{
					if (("${Source_Environment}" == 'Sandbox'))
					{
					env.TARGET = 'da669ba5-e2b1-41ff-9bff-f1350361776d'
					}
					else if (("${Source_Environment}" == 'QA'))
					{
					env.TARGET = '077667f6-3112-4c6d-b91d-c54a704c11cc'
					}
					else if (("${Source_Environment}" == 'Staging'))
					{
					env.TARGET = '6087b142b-4197-b3bb-9aec359be0ac'
					}
					else if (("${Source_Environment}" == 'QA1'))
					{
					env.TARGET = '5cf90f34-aa3b-09d17fea1f92'
					}
					else if (("${Source_Environment}" == 'QA2'))
					{
					env.TARGET = 'ceaa98c7-0ba9fe5-08c2e7db6cba'
					}
					else if (("${Source_Environment}" == 'QA3'))
					{
					env.TARGET = '10-b1fe-45dc-b9b8-514851debf9a'
					}
				}
			}
		}
	  stage('Register API') {
	   	    when {
            expression {
               VALUE_ONE == "${Actions}"
               
               }
            }
            
            steps {
                
            script {    
             sh 'code/api.sh'
             env.Actions  = 'policy'
              
               }
             
            }
        }
         stage('Apply Policy client-id-enforcement') {
	   	    when {
            expression {
               "${Actions}" == "policy"
              
               }
            }
            steps {
             sh 'code/api.sh'
            }
        }
    
        stage('Update API') {
	   	   when {
            expression {
               VALUE_TWO == "${Actions}"
               }
            }
            steps {
                 sh 'code/api.sh'
                }
            }
        stage('Promote API') {
	   	   when {
            expression {
               params.Actions == 'Promote' 
               }
            }
            steps {
             sh 'code/api.sh'
            }
        }    
    }
    /*	post {
        always {
                cleanWs()
        }
    }*/
}