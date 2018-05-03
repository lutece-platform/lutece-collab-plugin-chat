/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
package fr.paris.lutece.plugins.chat.service;

import fr.paris.lutece.plugins.chat.business.ChatEntry;
import fr.paris.lutece.plugins.chat.business.ChatRoom;
import fr.paris.lutece.plugins.chat.business.ChatUser;
import fr.paris.lutece.plugins.chat.business.RoomList;

/**
 * Titre : ChatService
 * Description : Class that provides basic services for chat clinkts
 * Copyright :    Copyright (c) 2001
 * Soci?t? :      Mairie de Paris
 * @author        SePD
 * @version 1.0
 */
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * This class manages chat users and rooms.
 */
public final class ChatService implements Runnable
{
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    // chat.properties
    private static final String PROPERTY_MAX_INACTIVITY = "chat.users.max.inactivity.seconds";
    private static final String PROPERTY_LANGUAGE = "chat.language";
    private static final String UNDEFINED = "undefined";
    private static final String PROPERTY_DEF_ADMIN_PASSWORD = "chatadmin";
    private static final String PROPERTY_DEF_BGCOLOR = "DDDDDD";
    private static final String PROPERTY_DEF_BTBGCOLOR = "555555";
    private static final String PROPERTY_DEF_BTFGCOLOR = "FFFFFF";
    private static final String PROPERTY_DEF_FDBGCOLOR = "FFFFFF";
    private static final String PROPERTY_FLOOD_DELAY_SECONDS = "chat.flood.delay.seconds";
    private static final String PROPERTY_FLOOD_MAX_DATA_SIZE = "chat.flood.max.data.size";
    private static final int PROPERTY_FLOOD_DELAY_SECONDS_DEF = 5;
    private static final int PROPERTY_FLOOD_MAX_DATA_SIZE_DEF = 300;
    private static final String PROPERTY_FLOOD_BOT_NAME = "chat.flood.bot.name";
    private static final String PROPERTY_FLOOD_BOT_NAME_DEF = "ChatSupervisor";
    private static final String PROPERTY_FLOOD_BOT_MESSAGE = "chat.flood.bot.message";
    private static final String PROPERTY_FLOOD_BOT_MESSAGE_DEF = "Excess flood";

    // IRC like commands
    private static final String COMMAND_MODE = "/MODE";
    private static final String COMMAND_WHOIS = "/WHOIS";
    private static final String COMMAND_KICK = "/KICK";
    private static final String COMMAND_ME = "/ME";
    private static final String COMMAND_NICK = "/NICK";
    private static final String COMMAND_MSG = "/MSG";
    private static final String COMMAND_TOPIC = "/TOPIC";
    private static final String COMMAND_AWAY = "/AWAY";
    private static final String COMMAND_PART = "/PART";
    private static final String COMMAND_QUIT = "/QUIT";

    // Specific commands
    private static final String COMMAND_OP = "/OP";
    private static final String COMMAND_BANLIST = "/BANLIST";

    // Notification messages
    private static final String MSG_OP = "op";
    private static final String MSG_QUIT = "quit";
    private static final String MSG_ENTER = "enter";
    private static final String MSG_TOPIC = "topic";
    private static final String MSG_AWAY = "away";
    private static final String MSG_COMEBACK = "comeback";
    private static final String MSG_NICK = "nick";
    private static final String MSG_KICK = "kick";
    private static final String MSG_KICKED = "kicked";
    private static final String MSG_BAN = "ban";
    private static final String MSG_BANNED = "banned";
    private static final String MSG_DEBAN = "deban";
    private static final String MSG_MODE = "mode";
    private static final String MSG_COMMAND_DENIED = "command.denied";
    private static final String MSG_COMMAND_INVALID_PARMS = "command.invalid.params";
    private static final String MSG_COMMAND_INVALID_USER = "command.invalid.user";
    private static final String MSG_COMMAND_UNKOWN = "command.unkown";
    private static final String MSG_INVALID_NICK = "invalid.nick";
    private static final String MSG_EXIT = "exit";
    private static final String MSG_BANNED_LIST_TITLE = "msg.banned.list.title";
    private static final String MSG_BANNED_LIST_TABLE = "msg.banned.list.table";
    private static final String MSG_USERS_INFOS_TITLE = "msg.users.infos.title";
    private static final String MSG_USERS_INFOS_IP = "msg.users.infos.ip";
    private static final String MSG_USERS_INFOS_LAST_ACCESS = "msg.users.infos.last.access";
    private static final String MSG_USERS_INFOS_ENTRANCE = "msg.users.infos.entrance";

    // Static variables
    private static RoomList _roomList;
    private Thread _timer;

    // Constructor ( private  singleton )

    /**
     * Creates a new ChatService object.
     */
    private ChatService(  )
    {
        // Create a timer to remove disconnected users
        _timer = new Thread( this );
        _timer.start(  );
    }

    /**
     * Room entering process
     *
     * @param request The Http request
     * @return One of the error codes defined in the ChatRoom class
     * @see fr.paris.lutece.plugins.chat.business.ChatRoom
         *
     */
    public static synchronized int doEnterRoom( HttpServletRequest request )
    {
        // Get the room posted in the form
        String strRoomName = request.getParameter( ChatConstantes.PARAM_ROOM );

        if ( ( strRoomName == null ) || ( strRoomName.length(  ) == 0 ) )
        {
            return ChatRoom.INVALID_ROOM;
        }

        // Attach the room to the user's session
        HttpSession session = request.getSession(  );
        session.setAttribute( ChatConstantes.ATTRIBUTE_ROOM_NAME, strRoomName );

        // R?cup?re le pseudo envoy? dans la requete du formulaire
        String strNickname = request.getParameter( ChatConstantes.PARAM_NICKNAME );

        // Attache le pseudo ? la session
        session.setAttribute( ChatConstantes.ATTRIBUTE_NICKNAME, strNickname );

        // Cr?e l'appUser et l'ajoute au salon
        ChatUser user = new ChatUser( strNickname );
        user.setIpAddress( request.getRemoteAddr(  ) );
        user.setHostName( request.getRemoteHost(  ) );
        user.setLastAccessTime( new Date(  ) );

        ChatRoom room = getRoom( request );

        if ( room == null )
        {
            return ChatRoom.INVALID_ROOM;
        }

        int nError = room.addUser( user );

        if ( nError != ChatRoom.USER_ADDED )
        {
            if ( nError == ChatRoom.USER_ALREADY_EXISTS )
            {
                while ( nError == ChatRoom.USER_ALREADY_EXISTS )
                {
                    user.setNickname( user.getNickname(  ) + "_" );
                    nError = room.addUser( user );
                }
            }
            else
            {
                return nError;
            }
        }

        room.addChatEntry( new ChatEntry( formatMsg( MSG_ENTER, strNickname ) ), null );

        return ChatRoom.USER_ADDED;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Rooms management

    /**
     * Get the room attached to the session
     *
     * @param request the servlet request
     * @return the room attached to the session
     */
    public static ChatRoom getRoom( HttpServletRequest request )
    {
        String strRoomName = request.getParameter( ChatConstantes.PARAM_ROOM );

        if ( strRoomName == null )
        {
            HttpSession session = request.getSession(  );
            strRoomName = (String) session.getAttribute( ChatConstantes.ATTRIBUTE_ROOM_NAME );
        }

        ChatRoom room = _roomList.getRoom( strRoomName );

        if ( room == null )
        {
            return null;
        }

        return room;
    }

    /**
     * Returns the rooms list
     *
     * @return the rooms ist as a RoomList object
     */
    public static RoomList getRoomList(  )
    {
        return _roomList;
    }

    /**
     * Initialize rooms
    
    
         */
    public static void initRooms(  )
    {
        // Create rooms
        _roomList = createRooms(  );
    }

    /**
     * Create rooms from the properties file
     * @return The created rooms list as a RoomList object
     */
    private static RoomList createRooms(  )
    {
        RoomList roomList = new RoomList(  );
        String strRoomName;
        String strRoomNameKey;
        String strRoomDescription;
        String strRoomDescriptionKey;

        int i = 1;

        while ( true )
        {
            strRoomNameKey = "chat.room" + i + ".name";
            strRoomName = AppPropertiesService.getProperty( strRoomNameKey, UNDEFINED );

            if ( strRoomName.equals( UNDEFINED ) )
            {
                break;
            }

            strRoomDescriptionKey = "chat.room" + i + ".description";
            strRoomDescription = AppPropertiesService.getProperty( strRoomDescriptionKey, UNDEFINED );

            ChatRoom room = new ChatRoom( strRoomName, strRoomDescription );

            // read other room attributes
            room.setAdminPassword( AppPropertiesService.getProperty( "chat.room" + i + ".admin.password",
                    PROPERTY_DEF_ADMIN_PASSWORD ) );
            room.setBgColor( AppPropertiesService.getProperty( "chat.room" + i + ".bgcolor", PROPERTY_DEF_BGCOLOR ) );
            room.setButtonBgColor( AppPropertiesService.getProperty( "chat.room" + i + ".btbgcolor",
                    PROPERTY_DEF_BTBGCOLOR ) );
            room.setButtonFgColor( AppPropertiesService.getProperty( "chat.room" + i + ".btfgcolor",
                    PROPERTY_DEF_BTFGCOLOR ) );
            room.setFieldBgColor( AppPropertiesService.getProperty( "chat.room" + i + ".fdbgcolor",
                    PROPERTY_DEF_FDBGCOLOR ) );
            roomList.addRoom( room );
            i++;
        }

        return roomList;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Users management

    /**
     * Remove all users with no activity since a given time
     *
     * @param room The room to remove users from
     */
    private void clearUsers( ChatRoom room )
    {
        // Parcours la liste des appUsers pour voir ceux qui ne sont plus actifs
        Date dateCurrent = new Date(  );
        int nMaxInactivity = AppPropertiesService.getPropertyInt( PROPERTY_MAX_INACTIVITY, 60 );
        Enumeration e = room.getUsers(  );

        while ( e.hasMoreElements(  ) )
        {
            ChatUser user = (ChatUser) e.nextElement(  );
            Date lastActivity = user.getLastAccessTime(  );

            if ( ( dateCurrent.getTime(  ) - lastActivity.getTime(  ) ) > ( nMaxInactivity * 1000 ) )
            {
                room.removeUser( user.getNickname(  ) );
                room.addChatEntry( new ChatEntry( formatMsg( MSG_QUIT, user.getNickname(  ) ) ), null );
            }
        }
    }

    /**
     * Ban flooders from a room.
     * @param room The room to ban users from
     */
    private void banFlooders( ChatRoom room )
    {
        // Parcours la liste des appUsers pour voir ceux qui ne sont plus actifs
        Enumeration e = room.getUsers(  );
        long lSeconds = AppPropertiesService.getPropertyInt( PROPERTY_FLOOD_DELAY_SECONDS,
                PROPERTY_FLOOD_DELAY_SECONDS_DEF );
        int nMaxSize = AppPropertiesService.getPropertyInt( PROPERTY_FLOOD_MAX_DATA_SIZE,
                PROPERTY_FLOOD_MAX_DATA_SIZE_DEF );
        String strFloodBotName = AppPropertiesService.getProperty( PROPERTY_FLOOD_BOT_NAME, PROPERTY_FLOOD_BOT_NAME_DEF );
        String strFloodBotMessage = AppPropertiesService.getProperty( PROPERTY_FLOOD_BOT_MESSAGE,
                PROPERTY_FLOOD_BOT_MESSAGE_DEF );

        while ( e.hasMoreElements(  ) )
        {
            ChatUser u = (ChatUser) e.nextElement(  );

            if ( u.getSentDataSizeSince( lSeconds ) > nMaxSize )
            {
                u.kick( formatMsg( MSG_KICKED, strFloodBotName, u.getNickname(  ), strFloodBotMessage ) );
                room.addChatEntry( new ChatEntry( formatMsg( MSG_KICK, strFloodBotName, u.getNickname(  ),
                            strFloodBotMessage ) ), null );
                room.banUser( u.getNickname(  ), formatMsg( MSG_BANNED ) );
                room.addChatEntry( new ChatEntry( formatMsg( MSG_BAN, strFloodBotName, u.getNickname(  ), "" ) ), null );
            }
        }
    }

    /**
     * Scan rooms for no activity users in order to remove them.
     * Use Runnable implementation
     */
    public void run(  )
    {
        Thread me = Thread.currentThread(  );

        while ( _timer == me )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
            }

            RoomList list = getRoomList(  );
            Enumeration e = list.getRooms(  );

            while ( e.hasMoreElements(  ) )
            {
                ChatRoom room = (ChatRoom) e.nextElement(  );
                clearUsers( room );
                banFlooders( room );
            }
        }
    }

    /**
     * Get nickname attached to the request or to user's session
     *
     * @param request the servlet request
         * @return The nickname
     */
    public static String getNickname( HttpServletRequest request )
    {
        String strNickname = request.getParameter( ChatConstantes.PARAM_NICKNAME );

        if ( strNickname == null )
        {
            HttpSession session = request.getSession(  );
            strNickname = (String) session.getAttribute( ChatConstantes.ATTRIBUTE_NICKNAME );
        }

        return strNickname;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message management

    /**
     *
     * @param request the HttpServlet request
     * @return if a new message is processed
     */
    public static boolean newMessage( HttpServletRequest request )
    {
        ChatRoom room = ChatService.getRoom( request );

        if ( room == null )
        {
            return false;
        }

        String strNickname = ChatService.getNickname( request );
        ChatUser user = room.getUser( strNickname );

        if ( ( user == null ) || user.isKicked(  ) )
        {
            return false;
        }

        // if the user was away, he's no longer away
        if ( user.isAway(  ) )
        {
            user.setAway( false );
            room.addChatEntry( user, new ChatEntry( formatMsg( MSG_COMEBACK, strNickname ) ), null );
        }

        String strMessage = request.getParameter( ChatConstantes.PARAM_MESSAGE );

        if ( ( strMessage != null ) && ( strMessage.length(  ) != 0 ) )
        {
            Message message = new Message( strMessage );

            if ( message.isCommand(  ) )
            {
                parseCommand( message, room, strNickname );
            }
            else
            {
                room.addChatEntry( user, new ChatEntry( strNickname, strMessage, ChatEntry.TYPE_MESSAGE ), null );
            }
        }

        return true;
    }

    /**
     * Commands parsing
     *
     * @param message The message
     * @param room The room
     * @param strNickname The nickname
     */
    private static void parseCommand( Message message, ChatRoom room, String strNickname )
    {
        ChatUser user = room.getUser( strNickname );

        if ( message.isCommand( COMMAND_AWAY ) )
        {
            setAway( room, user, message.getArgs(  ) );

            return;
        }
        else if ( message.isCommand( COMMAND_QUIT ) )
        {
            quit( room, user, message.getArgs(  ) );

            return;
        }
        else if ( message.isCommand( COMMAND_PART ) )
        {
            quit( room, user, "" );

            return;
        }

        if ( user.getMode(  ) == ChatUser.MODE_OP )
        {
            if ( message.isCommand( COMMAND_MODE ) )
            {
                changeUserMode( room, user, message.getArg1(  ), message.getArg2(  ) );
            }
            else if ( message.isCommand( COMMAND_WHOIS ) )
            {
                whoisUser( room, user, message.getArgs(  ) );
            }
            else if ( message.isCommand( COMMAND_KICK ) )
            {
                kickUser( room, user, message.getArg1(  ), message.getArg2(  ) );
            }
            else if ( message.isCommand( COMMAND_NICK ) )
            {
                changeNickname( room, user, message.getArgs(  ) );
            }
            else if ( message.isCommand( COMMAND_TOPIC ) )
            {
                setTopic( room, user, message.getArgs(  ) );
            }
            else if ( message.isCommand( COMMAND_MSG ) )
            {
                sendPrivateMessage( room, user, message.getArg1(  ), message.getArg2(  ) );
            }
            else if ( message.isCommand( COMMAND_ME ) )
            {
                room.addChatEntry( user,
                    new ChatEntry( strNickname, message.getArgs(  ), ChatEntry.TYPE_NOTIFICATION ), null );
            }
            else if ( message.isCommand( COMMAND_BANLIST ) )
            {
                banlist( room, user );
            }
            else
            {
                room.addChatEntry( user, new ChatEntry( formatMsg( MSG_COMMAND_UNKOWN, strNickname ) ),
                    room.getUser( strNickname ) );
            }
        }
        else
        {
            if ( message.isCommand( COMMAND_OP ) )
            {
                String strAdminPassword = message.getArgs(  );

                if ( strAdminPassword.equalsIgnoreCase( room.getAdminPassword(  ) ) )
                {
                    user.setMode( ChatUser.MODE_OP );
                    room.addChatEntry( new ChatEntry( strNickname, formatMsg( MSG_OP, strNickname ),
                            ChatEntry.TYPE_NOTIFICATION ), null );
                }
            }
            else
            {
                room.addChatEntry( user, new ChatEntry( formatMsg( MSG_COMMAND_DENIED, strNickname ) ), user );
            }
        }
    }

    /**
     * Changes the user mode
     *
     *@param room The chat room
     *@param userOperator  the chat room operator
     *@param strMode The user mode
     *@param strUser The user
     */
    private static void changeUserMode( ChatRoom room, ChatUser userOperator, String strMode, String strUser )
    {
        int nMode = getNewUserMode( strMode.toUpperCase(  ) );
        ChatUser user = null;

        switch ( nMode )
        {
            case ChatUser.MODE_USER:
            case ChatUser.MODE_VOICE:
            case ChatUser.MODE_OP:
                user = checkCommandUser( room, strUser, userOperator );

                if ( user != null )
                {
                    user.setMode( nMode );
                    room.addChatEntry( new ChatEntry( formatMsg( MSG_MODE, userOperator.getNickname(  ),
                                user.getNickname(  ), "" ) ), null );
                }

                break;

            case ChatUser.MODE_BAN:
                user = checkCommandUser( room, strUser, userOperator );

                if ( user != null )
                {
                    room.banUser( user.getNickname(  ), formatMsg( MSG_BANNED ) );
                    room.addChatEntry( new ChatEntry( formatMsg( MSG_BAN, userOperator.getNickname(  ),
                                user.getNickname(  ), "" ) ), null );
                }

                break;

            case ChatUser.MODE_DEBAN:
                room.debanUser( strUser );
                room.addChatEntry( new ChatEntry( formatMsg( MSG_DEBAN, "", strUser, "" ) ), userOperator );

                break;

            default:
                room.addChatEntry( new ChatEntry( formatMsg( MSG_COMMAND_INVALID_PARMS, "", user.getNickname(  ), "" ) ),
                    userOperator );
        }
    }

    /**
     * Returns the mode number from a string
     *
     *@param strMode  the mode string
     *@return the mode number
     */
    private static int getNewUserMode( String strMode )
    {
        if ( strMode.equals( "+O" ) )
        {
            return ChatUser.MODE_OP;
        }

        if ( strMode.equals( "+V" ) )
        {
            return ChatUser.MODE_VOICE;
        }

        if ( strMode.equals( "-O" ) || strMode.equals( "-V" ) )
        {
            return ChatUser.MODE_USER;
        }

        if ( strMode.equals( "+B" ) )
        {
            return ChatUser.MODE_BAN;
        }

        if ( strMode.equals( "-B" ) )
        {
            return ChatUser.MODE_DEBAN;
        }

        return -1;
    }

    /**
     * Returns informations about a user
     *
     *@param room The chat room
     *@param userOperator The user's operator
     *@param strUser The name of the user
     */
    private static void whoisUser( ChatRoom room, ChatUser userOperator, String strUser )
    {
        ChatUser user = checkCommandUser( room, strUser, userOperator );

        if ( user != null )
        {
            String strMessage = AppPropertiesService.getProperty( MSG_USERS_INFOS_TITLE ) + user.getNickname(  ) +
                "\n" + AppPropertiesService.getProperty( MSG_USERS_INFOS_IP ) + " (" + user.getIpAddress(  ) + ") " +
                user.getHostName(  ) + "\n" + AppPropertiesService.getProperty( MSG_USERS_INFOS_LAST_ACCESS ) +
                user.getLastAccessTime(  ).toString(  ) + "\n" +
                AppPropertiesService.getProperty( MSG_USERS_INFOS_ENTRANCE ) +
                new Date( user.getJoinTime(  ) ).toString(  );
            room.addChatEntry( new ChatEntry( "", strMessage, ChatEntry.TYPE_NOTIFICATION ), userOperator );
        }
    }

    /**
     * Kicks a user out of a chat room
     *
     * @param room The chat room
     * @param userOperator The operator
     * @param strUser The name of the user
     * @param strComment A string comment
     */
    private static void kickUser( ChatRoom room, ChatUser userOperator, String strUser, String strComment )
    {
        ChatUser user = checkCommandUser( room, strUser, userOperator );

        if ( user != null )
        {
            room.addChatEntry( new ChatEntry( formatMsg( MSG_KICK, userOperator.getNickname(  ), strUser, strComment ) ),
                null );
            user.kick( formatMsg( MSG_KICKED, userOperator.getNickname(  ), strUser, strComment ) );
        }
    }

    /**
     * Sends a private message from the chat operator to a user
     *
     * @param room The chat room
     * @param userOperator The chat operator
     * @param strUser The user name
     * @param strMessage The message
     */
    private static void sendPrivateMessage( ChatRoom room, ChatUser userOperator, String strUser, String strMessage )
    {
        ChatUser user = checkCommandUser( room, strUser, userOperator );

        if ( user != null )
        {
            String strSender = "<" + userOperator.getNickname(  ) + ">";
            room.addChatEntry( user, new ChatEntry( strSender, strMessage, ChatEntry.TYPE_MESSAGE ), user );
            room.addChatEntry( userOperator, new ChatEntry( strSender, strMessage, ChatEntry.TYPE_MESSAGE ),
                userOperator );
        }
    }

    /**
     * Changes a user nickname
     *
     * @param room The chat room
     * @param user The user
     * @param strNewNickname The new nickname
     */
    private static void changeNickname( ChatRoom room, ChatUser user, String strNewNickname )
    {
        String strNickname = user.getNickname(  );

        if ( room.changePseudo( strNickname, strNewNickname ) != ChatRoom.USER_ADDED )
        {
            room.addChatEntry( user, new ChatEntry( formatMsg( MSG_INVALID_NICK, strNewNickname ) ), user );
        }
        else
        {
            room.addChatEntry( user, new ChatEntry( formatMsg( MSG_NICK, strNickname, "", strNewNickname ) ), null );
        }
    }

    /**
     * Sets a user away
     *
     * @param room The chat room
     * @param user The user
     * @param strComment A string comment
     */
    private static void setAway( ChatRoom room, ChatUser user, String strComment )
    {
        if ( user.getMode(  ) == ChatUser.MODE_OP )
        {
            user.setMode( ChatUser.MODE_VOICE );
        }

        user.setAway( strComment );
        room.addChatEntry( user, new ChatEntry( formatMsg( MSG_AWAY, user.getNickname(  ), "", strComment ) ), null );
    }

    /**
     * Allows a user to leave the chat room
     *
     * @param room The chat room
     * @param user The user
     * @param strComment A comment
     */
    private static void quit( ChatRoom room, ChatUser user, String strComment )
    {
        room.addChatEntry( new ChatEntry( formatMsg( MSG_QUIT, user.getNickname(  ), "", strComment ) ), null );
        user.kick( formatMsg( MSG_EXIT ) );
    }

    /**
     * Sets the topic of the chat room
     *
     * @param room The chat room
     * @param user The user
     * @param strTopic The topic
     */
    private static void setTopic( ChatRoom room, ChatUser user, String strTopic )
    {
        room.setDescription( strTopic );
        room.addChatEntry( new ChatEntry( formatMsg( MSG_TOPIC, user.getNickname(  ), "", strTopic ) ), null );
    }

    /**
     * Adds an entry with the ban list
     *
     * @param room The chat room
     * @param userOperator The chat operator
     */
    private static void banlist( ChatRoom room, ChatUser userOperator )
    {
        Enumeration e = room.getBannedUsers(  );
        StringBuffer strList = new StringBuffer(  );
        strList.append( AppPropertiesService.getProperty( MSG_BANNED_LIST_TITLE ) + "\n" );
        strList.append( AppPropertiesService.getProperty( MSG_BANNED_LIST_TABLE ) + "\n" );

        SimpleDateFormat formatter = new SimpleDateFormat( "dd'/'MM'/'yyyy' 'HH':'mm", Locale.FRANCE );

        while ( e.hasMoreElements(  ) )
        {
            ChatUser user = (ChatUser) e.nextElement(  );
            strList.append( user.getIpAddress(  ) );
            strList.append( "    " );
            strList.append( formatter.format( user.getLastAccessTime(  ) ) );
            strList.append( "        " );
            strList.append( user.getNickname(  ) );
            strList.append( "\n" );
        }

        room.addChatEntry( new ChatEntry( "", strList.toString(  ), ChatEntry.TYPE_NOTIFICATION ), userOperator );
    }

    /**
     * Checks the user validity
     *
     * @param room The chat room
     * @param strUser The user name
     * @param userOperator The chat operator
         *
         * @return The object corespondig to the user name, null if the name is
         * invalid
     */
    private static ChatUser checkCommandUser( ChatRoom room, String strUser, ChatUser userOperator )
    {
        ChatUser user = room.getUser( strUser );

        if ( user == null )
        {
            room.addChatEntry( new ChatEntry( formatMsg( MSG_COMMAND_INVALID_USER, userOperator.getNickname(  ),
                        strUser, "" ) ), userOperator );
        }

        return user;
    }

    /**
     * Formats a message
     *
     * @param strMessageName The message name
     * @param strNickname The nickname
     * @param strUser The name of the user
     * @param strComment A comment
     * @return The formated message
     */
    private static String formatMsg( String strMessageName, String strNickname, String strUser, String strComment )
    {
        String strRessource = "chat.msg." + strMessageName;
        String strLanguage = AppPropertiesService.getProperty( PROPERTY_LANGUAGE, "" );

        if ( !strLanguage.equals( "" ) )
        {
            strRessource += ( "." + strLanguage );
        }

        String strPattern = AppPropertiesService.getProperty( strRessource );
        MessageFormat msgFormat = new MessageFormat( strPattern );
        Object[] msgArgs = { strNickname, strUser, strComment };

        return msgFormat.format( msgArgs );
    }

    /**
     * Formats a message
     *
     * @param strMessageName The message name
     * @param strNickname The nickname
     * @return The formated message
     */
    private static String formatMsg( String strMessageName, String strNickname )
    {
        return formatMsg( strMessageName, strNickname, "", "" );
    }

    /**
     * Formats a message
     *
     * @param strMessageName The message name
     * @return The formated message
     */
    private static String formatMsg( String strMessageName )
    {
        String strRessource = "chat.msg." + strMessageName;
        String strLanguage = AppPropertiesService.getProperty( PROPERTY_LANGUAGE, "" );

        if ( !strLanguage.equals( "" ) )
        {
            strRessource += ( "." + strLanguage );
        }

        return AppPropertiesService.getProperty( strRessource, "" );
    }
}


////////////////////////////////////////////////////////////////////////////////
// Class Message

/**
 * Utility class for message management :
 * <li>Extract commands and arguments from messages
 */
class Message
{
    private String _strCommand = ""; // command found in the message
    private String _strArgs = ""; // data found after the command
    private String _strArg1 = ""; // first argument found after the command
    private String _strArg2 = ""; // data found after the first argument
    private boolean _bCommand;// indicate whether the message is a command or not

    /**
     * Creates a new Message object.
     *
     * @param strMessage The text of the message
     */
    Message( String strMessage )
    {
        parseMessage( strMessage );
    }

    /**
     * Parse a message
         *
     * @param strMessage The message text
     */
    private void parseMessage( String strMessage )
    {
        if ( !strMessage.startsWith( "/" ) )
        {
            return;
        }

        _bCommand = true;

        int nPos = strMessage.indexOf( " " );

        if ( nPos != -1 )
        {
            _strCommand = strMessage.substring( 0, nPos ).toUpperCase(  );
            _strArgs = strMessage.substring( nPos + 1, strMessage.length(  ) );
            nPos = _strArgs.indexOf( " " );

            if ( nPos != -1 )
            {
                _strArg1 = _strArgs.substring( 0, nPos );
                _strArg2 = _strArgs.substring( nPos + 1, _strArgs.length(  ) );
            }
            else
            {
                _strArg1 = _strArgs;
            }
        }
        else
        {
            _strCommand = strMessage.toUpperCase(  );
        }
    }

    /**
     * @return true if the message contains a command.
     */
    boolean isCommand(  )
    {
        return _bCommand;
    }

    /**
     * Returns true if the message contains the specified command.
     * @param strCommand The command to check
     * @return true if the message contains the specified command
     */
    boolean isCommand( String strCommand )
    {
        return _strCommand.equals( strCommand );
    }

    /**
     * Return the command in upper case
         *
         * @return The command in upper case
     */
    String getCommand(  )
    {
        return _strCommand;
    }

    /**
     * Return all data found after the command
         *
         * @return The data
     */
    String getArgs(  )
    {
        return _strArgs;
    }

    /**
     * Return first argument found after the command
         *
         * @return The argument
     */
    String getArg1(  )
    {
        return _strArg1;
    }

    /**
     * Return data found after the first argument
         *
         * @return The data
     */
    String getArg2(  )
    {
        return _strArg2;
    }
}
