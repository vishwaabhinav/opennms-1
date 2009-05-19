/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 28, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.javamail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.config.common.JavamailProperty;
import org.opennms.netmgt.config.common.SendmailConfig;
import org.opennms.netmgt.config.common.SendmailMessage;
import org.opennms.netmgt.config.common.SendmailProtocol;
import org.springframework.mail.javamail.MimeMailMessage;

/**
 * Use this class for sending emailz.
 * 
 * Crude extension of JavaMailer
 * TODO: Improve class hierarchy.
 * 
 * TODO: Needs testing
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class JavaSendMailer extends JavaMailer2 {
    
    private Properties m_properties;
    
    private SendmailConfig m_config;
    private MimeMailMessage m_message;
    private Session m_session;
    
    /**
     * Constructs everything required to call send()
     * 
     * @param config
     *     SendmailConfig 
     * @param useJmProps
     *     A boolean representing the handling of the deprecated javamail-configuration.properties file.
     * @throws JavaMailerException
     */
    public JavaSendMailer(SendmailConfig config, boolean useJmProps) throws JavaMailerException {
        m_config = config;
        try {
            m_session = Session.getInstance(createProps(useJmProps), createAuthenticator());
            m_message = buildMimeMessage(config.getSendmailMessage());
            if (m_config.isDebug()) {
                m_session.setDebugOut(new PrintStream(new LoggingByteArrayOutputStream(log()), true));
            }
            m_session.setDebug(m_config.getDebug());

        } catch (IOException e) {
            throw new JavaMailerException("IO problem creating session", e);
        }
    }

    /**
     * Using this constructor implies overriding sendmail configuration with properties
     * from the deprecated javamail-configuration.properties file.
     * 
     * @param config
     * @throws JavaMailerException
     */
    public JavaSendMailer(SendmailConfig config) throws JavaMailerException {
        this(config, true);
    }
    
    public MimeMailMessage buildMimeMessage(SendmailMessage msg) {
        //no need to set the same object again
        if (m_config.getSendmailMessage() != msg) {
            m_config.setSendmailMessage(msg);
        }
        MimeMailMessage mimeMsg = new MimeMailMessage(new MimeMessage(m_session));
        mimeMsg.setFrom(m_config.getSendmailMessage().getFrom());
        mimeMsg.setTo(m_config.getSendmailMessage().getTo());
        mimeMsg.setSubject(m_config.getSendmailMessage().getSubject());
        return mimeMsg;
    }
    
    
    /**
     * Helper method to create an Authenticator based on Password Authentication
     * @return
     */
    public Authenticator createAuthenticator() {
        Authenticator auth;
        if (m_config.isUseAuthentication()) {
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(m_config.getUserAuth().getUserName(), m_config.getUserAuth().getPassword());
                }
            };
        } else {
            auth = null;
        }
        return auth;
    }

    private Properties createProps(boolean useJmProps) throws IOException {
        
        Properties props = generatePropsFromConfig(m_config.getJavamailPropertyCollection());
        configureProperties(props, useJmProps);
        
        //get rid of this
        return Session.getDefaultInstance(new Properties()).getProperties();
    }

    private Properties generatePropsFromConfig(List<JavamailProperty> javamailPropertyCollection) {
        Properties props = new Properties();
        for (JavamailProperty property : javamailPropertyCollection) {
            props.put(property.getName(), property.getValue());
        }
        return props;
    }

    /**
     * This method uses a properties file reader to pull in opennms styled javamail properties and sets
     * the actual javamail properties.  This is here to preserve the backwards compatibility but configuration
     * will probably change soon.
     * 
     * FIXME definitely will change soon, will be depreciated
     * 
     * @throws IOException
     */    
    private void configureProperties(Properties sendmailConfigDefinedProps, boolean useJmProps) {
        
        //this loads the properties from the old style javamail-configuration.properties
        //TODO: depreciate this
        Properties props = null;
        try {
            props = JavaMailerConfig.getProperties();
            
            /* These strange properties from javamail-configuration.properties need to be translated into actual javax.mail properties
             * FIXME: The precedence of the properties file vs. the SendmailConfiguration should probably be addressed here
             * FIXME: if using a valid sendmail config, it probably doesn't make since to use any of these properties
             */
            if (useJmProps) {
                m_config.setDebug(PropertiesUtils.getProperty(props, "org.opennms.core.utils.debug", m_config.getDebug()));
                m_config.getSendmailHost().setHost(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailHost", m_config.getSendmailHost().getHost()));
                m_config.setUseJmta(PropertiesUtils.getProperty(props, "org.opennms.core.utils.useJMTA", m_config.getUseJmta()));
                m_config.getSendmailProtocol().setMailer(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailer", m_config.getSendmailProtocol().getMailer()));
                m_config.getSendmailProtocol().setTransport(PropertiesUtils.getProperty(props, "org.opennms.core.utils.transport", m_config.getSendmailProtocol().getTransport()));
                m_config.getSendmailMessage().setFrom(PropertiesUtils.getProperty(props, "org.opennms.core.utils.fromAddress", m_config.getSendmailMessage().getFrom()));
                m_config.setUseAuthentication(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticate", m_config.getUseAuthentication()));
                m_config.getUserAuth().setUserName(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticateUser", m_config.getUserAuth().getUserName()));
                m_config.getUserAuth().setPassword(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticatePassword", m_config.getUserAuth().getPassword()));
                m_config.getSendmailProtocol().setMessageContentType(PropertiesUtils.getProperty(props, "org.opennms.core.utils.messageContentType", m_config.getSendmailProtocol().getMessageContentType()));
                m_config.getSendmailProtocol().setCharSet(PropertiesUtils.getProperty(props, "org.opennms.core.utils.charset", m_config.getSendmailProtocol().getCharSet()));
                m_config.getSendmailProtocol().setMessageEncoding(PropertiesUtils.getProperty(props, "org.opennms.core.utils.encoding", m_config.getSendmailProtocol().getMessageEncoding()));
                m_config.getSendmailProtocol().setStartTls(PropertiesUtils.getProperty(props, "org.opennms.core.utils.starttls.enable", m_config.getSendmailProtocol().isStartTls()));
                m_config.getSendmailProtocol().setQuitWait(PropertiesUtils.getProperty(props, "org.opennms.core.utils.quitwait", m_config.getSendmailProtocol().isQuitWait()));
                m_config.getSendmailHost().setPort(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpport", m_config.getSendmailHost().getPort()));
                m_config.getSendmailProtocol().setSslEnable(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpssl.enable", m_config.getSendmailProtocol().isSslEnable()));
            }
        } catch (IOException e) {
            log().info("configureProperties: could not load javamail.properties, continuing for is no longer required", e);
        }
        
        //this sets any javamail properties that were set in the SendmailConfig object
        if (props == null) {
            props = new Properties();
        }
        
        props.putAll(sendmailConfigDefinedProps);
        
        if (!props.containsKey("mail.smtp.auth")) {
            props.setProperty("mail.smtp.auth", String.valueOf(m_config.isUseAuthentication()));
        }
        if (!props.containsKey("mail.smtp.starttls.enable")) {
            props.setProperty("mail.smtp.starttls.enable", String.valueOf(m_config.getSendmailProtocol().isStartTls()));
        }
        if (!props.containsKey("mail.smtp.quitwait")) {
            props.setProperty("mail.smtp.quitwait", String.valueOf(m_config.getSendmailProtocol().isQuitWait()));
        }
        if (!props.containsKey("mail.smtp.port")) {
            props.setProperty("mail.smtp.port", String.valueOf(m_config.getSendmailHost().getPort()));
        }
        if (m_config.getSendmailProtocol().isSslEnable()) {
            if (!props.containsKey("mail.smtps.auth")) {
                props.setProperty("mail.smtps.auth", String.valueOf(m_config.isUseAuthentication()));
            }
            if (!props.containsKey("mail.smtps.socketFactory.class")) {
                props.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            if (!props.containsKey("mail.smtps.socketFactory.port")) {
                props.setProperty("mail.smtps.socketFactory.port", String.valueOf(m_config.getSendmailHost().getPort()));
            }
        }
        
        if (!props.containsKey("mail.smtp.quitwait")) {
            props.setProperty("mail.smtp.quitwait", String.valueOf(m_config.getSendmailProtocol().isQuitWait()));
        }
        
    }
    
    public void send() throws JavaMailerException {
        m_message.setText(m_config.getSendmailMessage().getBody());
        send(m_message);
    }
    
    private void send(MimeMailMessage message) throws JavaMailerException {
        Transport t = null;
        try {
            SendmailProtocol protoConfig = m_config.getSendmailProtocol();
            t = m_session.getTransport(protoConfig.getTransport());
            log().debug("for transport name '" + protoConfig.getTransport() + "' got: " + t.getClass().getName() + "@" + Integer.toHexString(t.hashCode()));

            LoggingTransportListener listener = new LoggingTransportListener(log());
            t.addTransportListener(listener);

            if (t.getURLName().getProtocol().equals("mta")) {
                // JMTA throws an AuthenticationFailedException if we call connect()
                log().debug("transport is 'mta', not trying to connect()");
            } else if (m_config.isUseAuthentication()) {
                log().debug("authenticating to " + m_config.getSendmailHost().getHost());
                t.connect(m_config.getSendmailHost().getHost(), (int)m_config.getSendmailHost().getPort(), m_config.getUserAuth().getUserName(), m_config.getUserAuth().getPassword());
            } else {
                log().debug("not authenticating to " + m_config.getSendmailHost().getHost());
                t.connect(m_config.getSendmailHost().getHost(), (int)m_config.getSendmailHost().getPort(), null, null);
            }

            t.sendMessage(message.getMimeMessage(), message.getMimeMessage().getAllRecipients());
            listener.assertAllMessagesDelivered();
        } catch (NoSuchProviderException e) {
            log().error("Couldn't get a transport: " + e, e);
            throw new JavaMailerException("Couldn't get a transport: " + e, e);
        } catch (MessagingException e) {
            log().error("Java Mailer messaging exception: " + e, e);
            throw new JavaMailerException("Java Mailer messaging exception: " + e, e);
        } finally {
            try {
                if (t != null && t.isConnected()) {
                    t.close();
                }
            } catch (MessagingException e) {
                throw new JavaMailerException("Java Mailer messaging exception on transport close: " + e, e);
            }
        }
    }



    public void setConfig(SendmailConfig config) {
        m_config = config;
    }

    public SendmailConfig getConfig() {
        return m_config;
    }

    public void setMessage(MimeMailMessage message) {
        m_message = message;
    }

    public MimeMailMessage getMessage() {
        return m_message;
    }

    public void setProperties(Properties properties) {
        m_properties = properties;
    }

    public Properties getProperties() {
        return m_properties;
    }

}
