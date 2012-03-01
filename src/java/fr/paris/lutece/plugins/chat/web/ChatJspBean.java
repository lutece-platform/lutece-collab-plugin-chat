/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.chat.web;

import fr.paris.lutece.plugins.chat.business.ChatEntry;
import fr.paris.lutece.plugins.chat.business.ChatRoom;
import fr.paris.lutece.plugins.chat.business.ChatUser;
import fr.paris.lutece.plugins.chat.service.ChatConstantes;
import fr.paris.lutece.plugins.chat.service.ChatService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


/**
 *
 */
public class ChatJspBean
{
    private static final long serialVersionUID = -6400074588556875395L;
    private static final String SEPARATOR = "\nend\n";
    private static final String CMD_ADD_USER = "ADD USER:";
    private static final String CMD_ADD_MSG = "ADD MESSAGE:";
    private static final String CMD_SET_TOPIC = "SET TOPIC:";
    private static final String CMD_NEW_PSEUDO = "NEW PSEUDO:";
    private static final String CMD_KICK = "KICK:";
    private static final String CONTENT_TYPE = "text/html";
    private static final String MESSAGE_RECEPTION = "chat.msg.message.received";
    private static final String MESSAGE_CONNEXION_CONFIRMATION = "chat.msg.connexion.established";
    private static final String MESSAGE_INVALID_ROOM = "chat.msg.invalid.room";
    private static final String MESSAGE_USER_BANNED = "chat.msg.user.banned";
    private static final String MESSAGE_USER_ALREADY_EXIST = "chat.msg.user.already.exist";
    private static final String MESSAGE_CONNEXION_FAILED = "chat.msg.connexion.failed";
    private static final String MESSAGE_USER_KICKED = "chat.msg.user.kicked";

    public String process( HttpServletRequest request )
    {
        String strReturn = null;
        System.out.println( request.getMethod(  ) + request.getParameterNames(  ).toString(  ) );

        if ( request.getMethod(  ).equalsIgnoreCase( "get" ) )
        {
            return getUserData( request );
        }
        else
        {
            if ( request.getParameter( ChatConstantes.PARAM_MESSAGE ) != null )
            {
                ChatService.newMessage( request );
                strReturn = AppPropertiesService.getProperty( MESSAGE_RECEPTION );
            }
            else
            {
                switch ( ChatService.doEnterRoom( request ) )
                {
                    case ChatRoom.USER_ADDED:
                        strReturn = buildMessage( AppPropertiesService.getProperty( MESSAGE_CONNEXION_CONFIRMATION ) );

                        break;

                    case ChatRoom.INVALID_ROOM:
                        strReturn = buildMessage( CMD_KICK, AppPropertiesService.getProperty( MESSAGE_INVALID_ROOM ) );

                        break;

                    case ChatRoom.USER_IS_BANNED:
                        strReturn = buildMessage( CMD_KICK, AppPropertiesService.getProperty( MESSAGE_USER_BANNED ) );

                        break;

                    case ChatRoom.USER_ALREADY_EXISTS:
                        strReturn = buildMessage( CMD_KICK,
                                AppPropertiesService.getProperty( MESSAGE_USER_ALREADY_EXIST ) );

                        break;

                    default:
                        strReturn = AppPropertiesService.getProperty( MESSAGE_CONNEXION_FAILED );

                        break;
                }
            }
        }

        return strReturn;
    }

    /**
     * Builds a message
     * @param strMessage The body of the message
     * @return The message string
     */
    private String buildMessage( String strMessage )
    {
        return buildMessage( CMD_ADD_MSG, strMessage );
    }

    /**
     * Builds a message
     * @param strCommand The message command
     * @param strMessage The body of the message
     * @return The message string
     */
    private String buildMessage( String strCommand, String strMessage )
    {
        StringBuffer strBuffer = new StringBuffer(  );
        strBuffer.append( strCommand );
        strBuffer.append( strMessage );
        strBuffer.append( SEPARATOR );

        return strBuffer.toString(  );
    }

    /**
     * Gets information about users
     * @param request The Http request
     * @return A string containing user information
     */
    private String getUserData( HttpServletRequest request )
    {
        StringBuffer strData = new StringBuffer(  );
        ChatRoom room = ChatService.getRoom( request );
        String strPseudo = ChatService.getNickname( request );
        ChatUser user = room.getUser( strPseudo );

        if ( user == null )
        {
            return buildMessage( CMD_KICK, AppPropertiesService.getProperty( MESSAGE_USER_KICKED ) );
        }

        if ( user.isKicked(  ) )
        {
            String strMessage = user.getKickComment(  );
            room.removeUser( strPseudo );

            return buildMessage( CMD_KICK, strMessage );
        }

        if ( user.hasNewPseudo(  ) )
        {
            room.removeOldPseudo( strPseudo );

            return buildMessage( CMD_NEW_PSEUDO, user.getNickname(  ) );
        }

        // List of the messages
        Enumeration entries = user.getChatEntries(  );

        while ( entries.hasMoreElements(  ) )
        {
            ChatEntry entry = (ChatEntry) entries.nextElement(  );

            if ( entry.getTime(  ) < user.getLastAccessTime(  ).getTime(  ) )
            {
                // The message came before the arrival of the user, then go to the next one
                continue;
            }

            switch ( entry.getType(  ) )
            {
                case ChatEntry.TYPE_MESSAGE:
                    strData.append( buildMessage( "<" + entry.getNickname(  ) + "> " + entry.getChatMessage(  ) ) );

                    break;

                case ChatEntry.TYPE_NOTIFICATION:
                    strData.append( buildMessage( entry.getChatMessage(  ) ) );

                    break;

                default:

                    // No data is appended
                    break;
            }
        }

        // List of the users
        Enumeration users = room.getUsers(  );

        while ( users.hasMoreElements(  ) )
        {
            ChatUser u = (ChatUser) users.nextElement(  );
            strData.append( CMD_ADD_USER );

            if ( u.getMode(  ) == ChatUser.MODE_OP )
            {
                strData.append( "@" );
            }
            else if ( u.getMode(  ) == ChatUser.MODE_VOICE )
            {
                strData.append( "+" );
            }

            strData.append( u.getNickname(  ) );

            if ( strPseudo.equals( u.getNickname(  ) ) )
            {
                strData.append( "*" );
            }

            if ( u.isAway(  ) )
            {
                if ( u.getAwayComment(  ) != null )
                {
                    strData.append( " (absent:" + u.getAwayComment(  ) + ")" );
                }
                else
                {
                    strData.append( " (absent)" );
                }
            }

            strData.append( SEPARATOR );
        }

        // Topic of the room
        strData.append( buildMessage( CMD_SET_TOPIC, room.getDescription(  ) ) );
        user.setLastAccessTime( new Date(  ) );

        return strData.toString(  );
    }
}
