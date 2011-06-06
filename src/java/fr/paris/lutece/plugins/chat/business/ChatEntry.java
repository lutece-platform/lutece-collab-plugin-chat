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

import java.util.Date;


/**
 * This class represents an entry in the chat.
 */
public class ChatEntry
{
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_NOTIFICATION = 1;
    private String _strNickname;
    private String _strChatMessage;
    private int _nEntryType;
    private Date _dateEntry;

    /**
     * Creates a new ChatEntry object.
     *
     * @param strNickname The nick name of the user
     * @param strChatMessage The message
     * @param nEntryType The type of entry
     */
    public ChatEntry( String strNickname, String strChatMessage, int nEntryType )
    {
        setChatEntry( strNickname, strChatMessage, nEntryType );
    }

    /**
     * Creates a new ChatEntry object.
     *
     * @param strNickname The nick name of the user
     * @param strChatMessage The message
     */
    public ChatEntry( String strNickname, String strChatMessage )
    {
        setChatEntry( strNickname, strChatMessage, TYPE_MESSAGE );
    }

    /**
     * Creates a new ChatEntry object.
     *
     * @param strChatMessage The message
     */
    public ChatEntry( String strChatMessage )
    {
        setChatEntry( "", strChatMessage, TYPE_NOTIFICATION );
    }

    /**
     * Sets an entry in the chat with the specified nick name, message, entry type and the current date.
     *
     * @param strNickname The nick name of the user
     * @param strChatMessage The message
     * @param nEntryType The type of entry
     */
    private void setChatEntry( String strNickname, String strChatMessage, int nEntryType )
    {
        _strNickname = strNickname;
        _strChatMessage = strChatMessage;
        _nEntryType = nEntryType;
        _dateEntry = new Date(  );
    }

    /**
     * Returns the nick name of this chat entry
     *
     * @return The nick name as a String
     */
    public String getNickname(  )
    {
        return _strNickname;
    }

    /**
     * Returns the message of this Chat entry
     *
     * @return The message as a String
     */
    public String getChatMessage(  )
    {
        return _strChatMessage;
    }

    /**
     * Returns the type of this chat entry
     *
     * @return The type as an int
     */
    public int getType(  )
    {
        return _nEntryType;
    }

    /**
     * Returns the date of this chat entry
     *
     * @return The date
     */
    public long getTime(  )
    {
        return _dateEntry.getTime(  );
    }
}
