/*
 * Copyright (c) 2002-2011, Mairie de Paris
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 * This class provides methods for the management of the chat users
 */
public class ChatUser
{
    public static final int MODE_USER = 0;
    public static final int MODE_VOICE = 1;
    public static final int MODE_OP = 2;
    public static final int MODE_BAN = 3;
    public static final int MODE_DEBAN = 4;
    private String _strNickname;
    private String _strIpAddress;
    private String _strHostName;
    private Vector _vChatEntries = new Vector(  );
    private int _nMaxMessages = 20;
    private Date _dateLastAccess;
    private Date _dateJoin;
    private int _nMode;
    private boolean _bAway;
    private String _strAwayComment;
    private boolean _bNewPseudo;
    private String _strKickComment;
    private boolean _bKicked;

    ////////////////////////////////////////////////////////////////////////////
    // Flood management
    private ArrayList _listSentData = new ArrayList(  );

    /**
     * Creates a new ChatUser object
     *
     * @param strNickname The nick name of the user
     */
    public ChatUser( String strNickname )
    {
        setNickname( strNickname );
        _dateJoin = new Date(  );
        _nMode = MODE_USER;
    }

    /**
     * Sets the nickname of this user with the specified parameter.
         * This method is declared final because it is called by the constructor.
     *
     * @param strNickname The type as an int
     */
    public final void setNickname( String strNickname )
    {
        _strNickname = strNickname.replace( ' ', '_' );
    }

    /**
     * Returns the nickname of this user
     *
     * @return The nick name
     */
    public String getNickname(  )
    {
        return _strNickname;
    }

    /**
     * Sets the ip address of this user with the specified parameter
     *
     * @param strIpAddress The String ip address
     */
    public void setIpAddress( String strIpAddress )
    {
        _strIpAddress = strIpAddress;
    }

    /**
     * Returns the ip address of this user
     *
     * @return The String ip address
     */
    public String getIpAddress(  )
    {
        return _strIpAddress;
    }

    /**
     * Sets the host name of this user with the specified parameter
     *
     * @param strHostName The String host name
     */
    public void setHostName( String strHostName )
    {
        _strHostName = strHostName;
    }

    /**
     * Returns the host name of this user
     *
     * @return The String host name
     */
    public String getHostName(  )
    {
        return _strHostName;
    }

    /**
     * Sets the host name of this user with the specified parameter
     *
     * @param dateLastAccess The String host name
     */
    public void setLastAccessTime( Date dateLastAccess )
    {
        _dateLastAccess = dateLastAccess;
    }

    /**
     * Returns the date of the last access of this user to the chat room
     *
     * @return The date last access
     */
    public Date getLastAccessTime(  )
    {
        return _dateLastAccess;
    }

    /**
     * Returns the time when this user entered the chat room
     *
     * @return The Long join time
     */
    public long getJoinTime(  )
    {
        return _dateJoin.getTime(  );
    }

    /**
     * Adds an entry to the ChatEntries Vector
     *
     * @param entry The ChatEntry object
     */
    public synchronized void addChatEntry( ChatEntry entry )
    {
        _vChatEntries.addElement( entry );

        if ( _vChatEntries.size(  ) > _nMaxMessages )
        {
            _vChatEntries.removeElementAt( 0 );
        }
    }

    /**
     * Return the entries of the chat
     *
     * @return The entries as an Enumeration object
     */
    public Enumeration getChatEntries(  )
    {
        return _vChatEntries.elements(  );
    }

    /**
     * Sets the mode of this user
     *
     * @param nMode nMode The mode of this user
     */
    public void setMode( int nMode )
    {
        _nMode = nMode;
    }

    /**
     * Return the mode of this user
     *
     * @return The user mode as an int
     */
    public int getMode(  )
    {
        return _nMode;
    }

    /**
     * Sets the attribute which define if this user is absent
     *
     * @param bAway The new value
     */
    public void setAway( boolean bAway )
    {
        _bAway = bAway;
        _strAwayComment = null;
    }

    /**
     * Sets this user as absent and add a comment
     *
     * @param strComment the comment of the absence
     */
    public void setAway( String strComment )
    {
        _bAway = true;
        _strAwayComment = strComment;
    }

    /**
     * Returns the attribute which define if this user is absent
     *
     * @return The boolean attribute _bAway
     */
    public boolean isAway(  )
    {
        return _bAway;
    }

    /**
     * Returns the absence comment
     *
     * @return The absence comment as a String
     */
    public String getAwayComment(  )
    {
        return _strAwayComment;
    }

    /**
     * Returns the boolean which define if the user has set a new pseudonym
     *
     * @return The boolean attribute _bNewPseudo
     */
    public boolean hasNewPseudo(  )
    {
        return _bNewPseudo;
    }

    /**
     * Set the boolean which define if the user has set a new pseudonym
     *
     * @param bNewPseudo the boolean which define if the user has set a new pseudonym
     */
    public void setNewPseudo( boolean bNewPseudo )
    {
        _bNewPseudo = bNewPseudo;
    }

    /**
     * Kicks this user out of the chat room
     *
     * @param strComment A comment about the kick
     */
    public void kick( String strComment )
    {
        _bKicked = true;
        _strKickComment = strComment;
    }

    /**
     * Returns the boolean which shows if this user has been kicked out the chat room
     *
     * @return the boolean attribte _bKicked
     */
    public boolean isKicked(  )
    {
        return _bKicked;
    }

    /**
     * Returns the comment shown when this user is kicked out of the chat room
     *
     * @return The comment as a String
     */
    public String getKickComment(  )
    {
        return _strKickComment;
    }

    /**
     * Adds text to the user's list of sent data
     *
     * @param strText The text to add
     */
    public void addSentData( String strText )
    {
        SentData sd = new SentData( strText );
        _listSentData.add( sd );
    }

    /**
     * Returns the amount of data sent for the last n seconds.
         *
     * @param lSeconds The number of seconds
     * @return The size of the sent data
     */
    public int getSentDataSizeSince( long lSeconds )
    {
        long lTime = new Date(  ).getTime(  ) - ( 1000 * lSeconds );
        Iterator i = _listSentData.iterator(  );
        int nSize = 0;

        while ( i.hasNext(  ) )
        {
            SentData sd = (SentData) i.next(  );

            if ( sd._date.getTime(  ) > lTime )
            {
                nSize += ( sd._text.length(  ) + 50 );
            }
        }

        return nSize;
    }

    /**
     * This class represents sent data.
     */
    private class SentData
    {
        String _text;
        Date _date;

        /**
         * Creates a new SentData object.
         *
         * @param strText  @param DOCUMENT ME
         */
        SentData( String strText )
        {
            _text = strText;
            _date = new Date(  );
        }
    }
}
