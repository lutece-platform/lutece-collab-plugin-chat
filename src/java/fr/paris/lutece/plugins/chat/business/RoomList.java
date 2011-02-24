/*
 * Copyright (c) 2002-2009, Mairie de Paris
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

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * This class provides methods for the management of the rooms list
 */
public class RoomList
{
    private Hashtable _chatRooms = new Hashtable(  );

    /**
     * Add a room in the list
     *
     * @param room The chat room to add
     */
    public void addRoom( ChatRoom room )
    {
        _chatRooms.put( room.getName(  ), room );
    }

    /**
     * Returns the chat room of the list whose name is the one specified in parameter
     *
     * @param strName The name of the room to get in the list
     * @return The ChatRoom object
     */
    public ChatRoom getRoom( String strName )
    {
        return (ChatRoom) _chatRooms.get( strName );
    }

    /**
     * Returns the list of the Rooms
     *
     * @return The list of the rooms
     */
    public Enumeration getRooms(  )
    {
        return _chatRooms.elements(  );
    }

    /**
     * Remove rooms in the list
     *
     * @param rooms An array of rooms to remove in the list
     */
    public void removeRooms( String[] rooms )
    {
        for ( int i = 0; i < rooms.length; i++ )
        {
            _chatRooms.remove( rooms[i] );
        }
    }
}
