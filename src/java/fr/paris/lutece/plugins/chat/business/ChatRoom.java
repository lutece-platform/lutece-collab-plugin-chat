/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
package fr.paris.lutece.plugins.chat.business;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class provides methods for the ChatRoom objects
 */
public class ChatRoom
{
    public static final int INVALID_ROOM = -1;
    public static final int USER_ADDED = 0;
    public static final int USER_ALREADY_EXISTS = 1;
    public static final int USER_IS_BANNED = 2;
    private String _strName;
    private String _strDescription;
    private String _strAdminPassword;
    private String _strBgColor;
    private String _strButtonBgColor;
    private String _strButtonFgColor;
    private String _strFieldBgColor;
    private Hashtable _htUsers = new Hashtable(  );
    private Hashtable _htBannedUsers = new Hashtable(  );
    private Vector _vRoomEntries = new Vector(  );

    /**
     * Creates a new ChatRoom object.
     *
     * @param strName The name of the room
     * @param strDescription The description of the room
     */
    public ChatRoom( String strName, String strDescription )
    {
        _strName = strName;
        _strDescription = strDescription;
    }

    /**
     * Adds an entry to this chat room
     *
     * @param entry The chat entry to add to the room
     * @param userRecipient The user which receives the message
     */
    public synchronized void addChatEntry( ChatEntry entry, ChatUser userRecipient )
    {
        addChatEntry( null, entry, userRecipient );
    }

    /**
     * Adds an entry to this chat room
     *
     * @param userSender The user which sends the message
     * @param entry The chat entry to add to the room
     * @param userRecipient The user which receives the message
     */
    public synchronized void addChatEntry( ChatUser userSender, ChatEntry entry, ChatUser userRecipient )
    {
        if ( userRecipient != null )
        {
            userRecipient.addChatEntry( entry );
        }
        else
        {
            // Send to all
            Enumeration e = getUsers(  );

            while ( e.hasMoreElements(  ) )
            {
                ChatUser u = (ChatUser) e.nextElement(  );
                u.addChatEntry( entry );
            }
        }

        if ( userSender != null )
        {
            userSender.addSentData( entry.getChatMessage(  ) );
        }
    }

    /**
     * Checks if the user specified in parameter is not already existent in the room and if it is not banned. If not,
     * it is added to the room. It returns an int code which describes the status of the user.
     *
     * @param user The user to add to the room
     * @return an int code which describes the result of the process
     */
    public synchronized int addUser( ChatUser user )
    {
        if ( _htUsers.containsKey( user.getNickname(  ) ) )
        {
            return USER_ALREADY_EXISTS;
        }

        if ( _htBannedUsers.containsKey( user.getIpAddress(  ) ) )
        {
            return USER_IS_BANNED;
        }

        _htUsers.put( user.getNickname(  ), user );

        return USER_ADDED;
    }

    /**
     * Modifies the nick name of the user
     *
     * @param strPseudo The old pseudo
     * @param strNewNickname The new pseudo
     * @return The result of the process
     */
    public int changePseudo( String strPseudo, String strNewNickname )
    {
        ChatUser user = getUser( strPseudo );
        user.setNickname( strNewNickname );

        int nError = addUser( user );

        if ( nError != USER_ADDED )
        {
            return nError;
        }

        user.setNewPseudo( true );

        return USER_ADDED;
    }

    /**
     * Removes the user which has this pseudo in the room.
     *
     * @param strPseudo The pseudo of the user to delete from the room
     */
    public void removeOldPseudo( String strPseudo )
    {
        ChatUser user = getUser( strPseudo );
        user.setNewPseudo( false );
        removeUser( strPseudo );
    }

    /**
     * Returns the numbers of users in the room
     *
     * @return The number of users as an int
     */
    public int getUserCount(  )
    {
        return _htUsers.size(  );
    }

    /**
     * Removes the user which has this pseudo in the room.
     *
     * @param strPseudo The pseudo of the user to delete from the room
     */
    public void removeUser( String strPseudo )
    {
        _htUsers.remove( strPseudo );
    }

    /**
     * Bannes a user from the room and comments it.
     *
     * @param strPseudo The pseudo to ban
     * @param strComment The comment to add to this ban
     */
    public void banUser( String strPseudo, String strComment )
    {
        ChatUser user = (ChatUser) _htUsers.get( strPseudo );
        _htBannedUsers.put( user.getIpAddress(  ), user );
        user.kick( strComment );
    }

    /**
     * Removes an ip address from the list of those which are banned from the room
     *
     * @param strIpAddress The ip address to authorize again
     */
    public void debanUser( String strIpAddress )
    {
        _htBannedUsers.remove( strIpAddress );
    }

    /**
     * Returns the list of the users of this room
     *
     * @return The enumeration which contains the users of the room
     */
    public Enumeration getUsers(  )
    {
        return _htUsers.elements(  );
    }

    /**
     * The list of the entries of the room
     *
     * @return A vector which contains the list of the entries
     */
    public Vector getRoomEntries(  )
    {
        return _vRoomEntries;
    }

    /**
     * Returns the list of the users banned from this room
     *
     * @return An enumeration of the banned users
     */
    public Enumeration getBannedUsers(  )
    {
        return _htBannedUsers.elements(  );
    }

    /**
     * Retruns the description of this room
     *
     * @return The string description of this room
     */
    public String getDescription(  )
    {
        return _strDescription;
    }

    /**
     * The name of this room
     *
     * @return The string name of this room
     */
    public String getName(  )
    {
        return _strName;
    }

    /**
     * Sets the last access to the room of the user which corresponds to the pseudo specified in parameter
     *
     * @param strPseudo The user's pseudo
     */
    public void setLastAccessTime( String strPseudo )
    {
        ChatUser user = (ChatUser) _htUsers.get( strPseudo );
        user.setLastAccessTime( new Date(  ) );
    }

    /**
     * Returns the ChatUser object whose pseudo is specified in parameter
     *
     * @param strPseudo The pseudo of the user to get
     * @return The ChatUser object
     */
    public ChatUser getUser( String strPseudo )
    {
        return (ChatUser) _htUsers.get( strPseudo );
    }

    /**
     * Sets the password of the room
     *
     * @param strAdminPassword The new password of the admin
     */
    public void setAdminPassword( String strAdminPassword )
    {
        _strAdminPassword = strAdminPassword;
    }

    /**
     * The password of the admin
     *
     * @return The admin password
     */
    public String getAdminPassword(  )
    {
        return _strAdminPassword;
    }

    /**
     * Sets the description of this room whith the specified string
     *
     * @param strDescription The description of the room
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Sets the background color of the room
     *
     * @param strBgColor The background color
     */
    public void setBgColor( String strBgColor )
    {
        _strBgColor = strBgColor;
    }

    /**
     * Returns the background color of the room
     *
     * @return The background color of the room as a String
     */
    public String getBgColor(  )
    {
        return _strBgColor;
    }

    /**
     * Sets the button background color
     *
     * @param strButtonBgColor The button's background color
     */
    public void setButtonBgColor( String strButtonBgColor )
    {
        _strButtonBgColor = strButtonBgColor;
    }

    /**
     * Returns the background color of the button
     *
     * @return The background color of the button as a String
     */
    public String getButtonBgColor(  )
    {
        return _strButtonBgColor;
    }

    /**
     * Sets the foreground color of the button
     *
     * @param strButtonFgColor The button's foreground color
     */
    public void setButtonFgColor( String strButtonFgColor )
    {
        _strButtonFgColor = strButtonFgColor;
    }

    /**
     * Returns the foreground color of the button
     *
     * @return The foreground color of the button as a String
     */
    public String getButtonFgColor(  )
    {
        return _strButtonFgColor;
    }

    /**
     * Sets the background color of the text field
     *
     * @param strFieldBgColor The text field's background color
     */
    public void setFieldBgColor( String strFieldBgColor )
    {
        _strFieldBgColor = strFieldBgColor;
    }

    /**
     * Returns the background color of the text field
     *
     * @return The background color of the text field as a String
     */
    public String getFieldBgColor(  )
    {
        return _strFieldBgColor;
    }
}
