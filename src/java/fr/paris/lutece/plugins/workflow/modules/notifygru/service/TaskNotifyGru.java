/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.notifygru.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import fr.paris.lutece.plugins.workflow.modules.notifygru.business.TaskNotifyGruConfig;
import fr.paris.lutece.plugins.workflow.modules.notifygru.utils.constants.NotifyGruConstants;
import fr.paris.lutece.plugins.workflow.modules.notifygru.utils.constants.TaskNotifyGruConstants;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.SimpleTask;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


/**
 *
 * TaskNotifyGru
 *
 */
public class TaskNotifyGru extends SimpleTask
{
    // TEMPLATES
    /* private static final String TEMPLATE_TASK_NOTIFY_DESK = "admin/plugins/workflow/modules/notifygru/task_notify_gru_desk.html";
     private static final String TEMPLATE_TASK_NOTIFY_AGENT = "admin/plugins/workflow/modules/notifygru/task_notify_gru_agent.html";
     private static final String TEMPLATE_TASK_NOTIFY_MAIL = "admin/plugins/workflow/modules/notifygru/task_notify_gru_mail.html";
     private static final String TEMPLATE_TASK_NOTIFY_SMS = "admin/plugins/workflow/modules/notifygru/task_notify_gru_sms.html";*/

    // SERVICES
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    @Named( TaskNotifyGruConfigService.BEAN_SERVICE )
    private ITaskConfigService _taskNotifyGruConfigService;
    private AbstractServiceProvider _notifyGruService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
    {
        //ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey(nIdResourceHistory);
        TaskNotifyGruConfig config = _taskNotifyGruConfigService.findByPrimaryKey( this.getId(  ) );

        if ( ( config != null ) && ServiceConfigTaskForm.isBeanExiste( config.getIdSpringProvider(  ) ) )
        {
            _notifyGruService = SpringContextService.getBean( config.getIdSpringProvider(  ) );
            
            String strUserEmail = _notifyGruService.getUserEmail(nIdResourceHistory);
            String strUserGuid = _notifyGruService.getUserGuid(nIdResourceHistory);
            String strStatus = _notifyGruService.getStatus(nIdResourceHistory);
            
            Map<String, Object> modelInfoHelp = new HashMap<String, Object>(  );
            String strInfosHelp = _notifyGruService.getInfosHelp(request, modelInfoHelp);
            Object InfoRessource = _notifyGruService.getInfos(nIdResourceHistory);
            
            

        /*    Resource resource = (Resource) _notifyGruService.getInfos( 0 );
            Map<String, Object> modelMessageContent = new HashMap<String, Object>(  );
            modelMessageContent.put( TaskNotifyGruConstants.MARK_RESOURCE, resource );

            Map<String, Object> model = new HashMap<String, Object>(  );

            model.put( NotifyGruConstants.MARK_CONFIG, config );

            JSONObject fluxJson = new JSONObject(  );
            JSONObject notificationJson = new JSONObject(  );

            notificationJson.accumulate( TaskNotifyGruConstants.MARK_USER_GUID, _notifyGruService.getUserGuid(nIdResourceHistory));
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_USER_EMAIL, resource.getEmail(  ) );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFICATION_ID, "" );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFICATION_DATE, "" );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFICATION_TYPE, resource.getNotificationType(  ) );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_ID_DEMAND, resource.getIdDemand(  ) );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_ID_DEMAND_TYPE, resource.getIdDemandType(  ) );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_DEMAND_MAX_STEP, resource.getDemandMaxStep(  ) );
            notificationJson.accumulate( TaskNotifyGruConstants.MARK_DEMAND_USER_CURRENT_STEP,
                resource.getDemandUserCurrentStep(  ) );

            boolean bIsNotifyByDesk = config.isActiveOngletGuichet(  );
            boolean bIsNotifyByViewAgent = config.isActiveOngletAgent(  );
            boolean bIsNotifyByEmail = config.isActiveOngletEmail(  );
            boolean bIsNotifyBySms = config.isActiveOngletSMS(  );

            if ( bIsNotifyByDesk )
            {
                //user_dashboard
                JSONObject userDashBoardJson = new JSONObject(  );
                HtmlTemplate t = AppTemplateService.getTemplateFromStringFtl( config.getMessageGuichet(  ), locale,
                        modelMessageContent );
                userDashBoardJson.accumulate( TaskNotifyGruConstants.MARK_STATUS_TEXT_USERDASHBOARD,
                    resource.getStatusText(  ) );
                userDashBoardJson.accumulate( TaskNotifyGruConstants.MARK_ID_STATUS_CRM_USERDASHBOARD,
                    resource.getIdStatusCrm(  ) );
                userDashBoardJson.accumulate( TaskNotifyGruConstants.MARK_MESSAGE_USERDASHBOARD, t.getHtml(  ) );
                notificationJson.accumulate( TaskNotifyGruConstants.MARK_USER_DASHBOARD, userDashBoardJson );
            }

            if ( bIsNotifyByEmail )
            {
                //user_email
                JSONObject userEmailJson = new JSONObject(  );
                HtmlTemplate t = AppTemplateService.getTemplateFromStringFtl( config.getMessageEmail(  ), locale,
                        modelMessageContent );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_SENDER_NAME, config.getSenderNameEmail(  ) );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_SENDER_EMAIL, MailService.getNoReplyEmail(  ) );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_RECIPIENT, resource.getEmail(  ) );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_SUBJECT, config.getSubjectEmail(  ) );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_MESSAGE_EMAIL, t.getHtml(  ) );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_CC, "" );
                userEmailJson.accumulate( TaskNotifyGruConstants.MARK_CCI, "" );
                notificationJson.accumulate( TaskNotifyGruConstants.MARK_TAB_USER_MAIL, userEmailJson );
            }

            if ( bIsNotifyBySms )
            {
                //user_sms
                JSONObject smsJson = new JSONObject(  );
                HtmlTemplate t = AppTemplateService.getTemplateFromStringFtl( config.getMessageSMS(  ), locale,
                        modelMessageContent );
                smsJson.accumulate( TaskNotifyGruConstants.MARK_PHONE_NUMBER, resource.getPhoneNumber(  ) );
                smsJson.accumulate( TaskNotifyGruConstants.MARK_MESSAGE_SMS, t.getHtml(  ) );
                notificationJson.accumulate( TaskNotifyGruConstants.MARK_USER_SMS, smsJson );
            }

            if ( bIsNotifyByViewAgent )
            {
                //backoffice_logging
                JSONObject backOfficeLogginJson = new JSONObject(  );
                HtmlTemplate tViewAgent = AppTemplateService.getTemplateFromStringFtl( config.getMessageAgent(  ),
                        locale, modelMessageContent );
                backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_MESSAGE_BACK_OFFICE_LOGGING,
                    tViewAgent.getHtml(  ) );
                backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_STATUS_TEXT_BACK_OFFICE_LOGGING,
                    resource.getStatusText(  ) );
                backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_ID_STATUS_CRM_BACK_OFFICE_LOGGING,
                    resource.getIdStatusCrm(  ) );

                if ( bIsNotifyByDesk )
                {
                    HtmlTemplate tDashboard = AppTemplateService.getTemplateFromStringFtl( config.getMessageGuichet(  ),
                            locale, modelMessageContent );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFIED_ON_DASHBOARD, 1 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_DISPLAY_LEVEL_DASHBOARD_NOTIFICATION, 2 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_VIEW_DASHBOARD_NOTIFICATION,
                        TaskNotifyGruConstants.MARK_DISPLAY_MESSAGE + tDashboard.getHtml(  ) );
                }

                if ( bIsNotifyByEmail )
                {
                    HtmlTemplate tEmail = AppTemplateService.getTemplateFromStringFtl( config.getMessageEmail(  ),
                            locale, modelMessageContent );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFIED_BY_EMAIL, 1 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_DISPLAY_LEVEL_EMAIL_NOTIFICATION, 2 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_VIEW_EMAIL_NOTIFICATION,
                        TaskNotifyGruConstants.MESSAGE_DISPLAY_EMAIL + resource.getEmail(  ) +
                        TaskNotifyGruConstants.MESSAGE_DISPLAY_OBJECT + config.getSubjectEmail(  ) +
                        TaskNotifyGruConstants.MESSAGE_DISPLAY_MESSAGE_EMAIL + tEmail.getHtml(  ) );
                }

                if ( bIsNotifyBySms )
                {
                    HtmlTemplate tSms = AppTemplateService.getTemplateFromStringFtl( config.getMessageSMS(  ), locale,
                            modelMessageContent );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFIED_BY_SMS, 1 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_DISPLAY_LEVEL_SMS_NOTIFICATION, 1 );
                    backOfficeLogginJson.accumulate( TaskNotifyGruConstants.MARK_VIEW_SMS_NOTIFICATION,
                        TaskNotifyGruConstants.MESSAGE_DISPLAY_SMS + resource.getPhoneNumber(  ) +
                        TaskNotifyGruConstants.MESSAGE_DISPLAY_MESSAGE_SMS + tSms.getHtml(  ) );
                }

                notificationJson.accumulate( TaskNotifyGruConstants.MARK_BACK_OFFICE_LOGGING, backOfficeLogginJson );
            }

            //notification
            fluxJson.accumulate( TaskNotifyGruConstants.MARK_NOTIFICATION, notificationJson );

            String strJsontoESB = fluxJson.toString( 2 );

            try
            {
                Client client = Client.create(  );
                

                WebResource webResource = client.resource( AppPropertiesService.getProperty( 
                            TaskNotifyGruConstants.URL_ESB ) );
 

                
   
                ClientResponse response = webResource.type(TaskNotifyGruConstants.CONTENT_FORMAT)
                                                     .header(HttpHeaders.AUTHORIZATION, "Bearer "+AppPropertiesService.getProperty( 
                                                                 TaskNotifyGruConstants.TOKEN ))
                                                     .accept(MediaType.APPLICATION_JSON)
                                                     .post( ClientResponse.class,
                                                     strJsontoESB );

                if ( response.getStatus(  ) != 201 )
                {
                    throw new RuntimeException( TaskNotifyGruConstants.ERROR_MESSAGE + response.getStatus(  ) );
                }

                String output = response.getEntity( String.class );
            }
            catch ( Exception e )
            {
                e.getMessage();
            }
            */
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveConfig(  )
    {
        _taskNotifyGruConfigService.remove( this.getId(  ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( Locale locale )
    {
        TaskNotifyGruConfig config = _taskNotifyGruConfigService.findByPrimaryKey( this.getId(  ) );

        if ( config != null )
        {
            return new String( I18nService.getLocalizedString(NotifyGruConstants.TITLE_NOTIFY, locale));
        }

        return new String( I18nService.getLocalizedString(NotifyGruConstants.TITLE_NOTIFY, locale) );
    }
}
