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
package fr.paris.lutece.plugins.chat.web;

import fr.paris.lutece.plugins.chat.business.ChatRoom;
import fr.paris.lutece.plugins.chat.business.RoomList;
import fr.paris.lutece.plugins.chat.service.ChatConstantes;
import fr.paris.lutece.plugins.chat.service.ChatService;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides chat pages.
 */
public class ChatApp implements XPageApplication
{
    private static final String TEMPLATE_CHAT_APPLET = "skin/plugins/chat/applet.html";
    private static final String TEMPLATE_CHAT_ENTER = "skin/plugins/chat/chat_enter.html";
    private static final String TEMPLATE_ROOM_LIST_ROW = "skin/plugins/chat/room_list_row.html";
    private static final String BOOKMARK_NICKNAME = "@nickname@";
    private static final String BOOKMARK_ROOM = "@room@";
    private static final String BOOKMARK_ROOM_DESCRIPTION = "@room_description@";
    private static final String BOOKMARK_CHECKED = "@checked@";
    private static final String BOOKMARK_ROWS = "@rooms_list_row@";
    private static final String BOOKMARK_GUESTS_COUNT = "@users_count@";
    private static final String BOOKMARK_BGCOLOR = "@bgcolor@";
    private static final String BOOKMARK_BTBGCOLOR = "@btbgcolor@";
    private static final String BOOKMARK_BTFGCOLOR = "@btfgcolor@";
    private static final String BOOKMARK_FDBGCOLOR = "@fdbgcolor@";
    private static final String PARAM_ACCESS_CODE = "access_code";
    private static final String PROPERTY_CHAT_PAGE_TITLE = "chat.page.title";
    private static final String PROPERTY_CHAT_PAGE_PATH_LABEL = "chat.page.pathLabel";
    private static final String PROPERTY_CHAT_ERROR_INVALID_ROOM = "chat.msg.invalid.room";

    /**
     * Creates a new ChatPage object
     */
    public ChatApp(  )
    {
    }

    /**
     * Returns the content of the xpage according to the parameters of the http request and the mode : <br> This method
     * implements the XPage interface
     *
     * @param request The http request
     * @param nMode The mode in which the page is called
     * @param plugin The plugin
     * @return The html content of the page
     * @throws SiteMessageException If an exception occurs
     */
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin )
        throws SiteMessageException
    {
        XPage page = new XPage(  );
        String strTitle = AppPropertiesService.getProperty( PROPERTY_CHAT_PAGE_TITLE );
        String strPathLabel = AppPropertiesService.getProperty( PROPERTY_CHAT_PAGE_PATH_LABEL );
        page.setTitle( strTitle );
        page.setPathLabel( strPathLabel );

        String strNickname = request.getParameter( ChatConstantes.PARAM_NICKNAME );

        // the pseudonym is stored in the http request
        if ( strNickname != null )
        {
            page.setContent( getAppletPage( request, strNickname ) );

            return page;
        }

        // the pseudonym is not stored in the http request
        String strAccessCode = request.getParameter( PARAM_ACCESS_CODE );

        // The access code is stored in the http request
        if ( strAccessCode != null )
        {
            page.setContent( getAppletPage( request, strAccessCode ) );

            return page;
        }

        // The access code is not stored in the http request, display the login page
        page.setContent( getLoginPage(  ) );

        return page;
    }

    /**
     * Returns the page which contains the chat applet
     *
     * @param request The http request which contains the connexion parameters
     * @param strNickname The connexion name (pseudonym)
     * @return The html code of the page which contains the chat applet
     */
    private String getAppletPage( HttpServletRequest request, String strNickname )
    {
        ChatRoom room = ChatService.getRoom( request );

        if ( room == null )
        {
            return AppPropertiesService.getProperty( PROPERTY_CHAT_ERROR_INVALID_ROOM );
        }

        String strRoom = request.getParameter( ChatConstantes.PARAM_ROOM );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CHAT_APPLET );
        template.substitute( BOOKMARK_NICKNAME, strNickname );
        template.substitute( BOOKMARK_ROOM, strRoom );
        template.substitute( BOOKMARK_BGCOLOR, room.getBgColor(  ) );
        template.substitute( BOOKMARK_BTBGCOLOR, room.getButtonBgColor(  ) );
        template.substitute( BOOKMARK_BTFGCOLOR, room.getButtonFgColor(  ) );
        template.substitute( BOOKMARK_FDBGCOLOR, room.getFieldBgColor(  ) );

        return template.getHtml(  );
    }

    /**
     * Returns the page which contains the chat applet (form with access code and password fields or form with
     * pseudonym filed)
     *
     * @return The html content of the access page to the chat
     */
    private String getLoginPage(  )
    {
        // Choose of the access form : access with authentification or free access
        String strTemplate = TEMPLATE_CHAT_ENTER;
        HtmlTemplate template = AppTemplateService.getTemplate( strTemplate );

        // Scan of the rooms list to create the table
        RoomList roomList = ChatService.getRoomList(  );
        Enumeration rooms = roomList.getRooms(  );
        boolean bFirst = true;
        StringBuffer strLines = new StringBuffer(  );
        HtmlTemplate templateLine = AppTemplateService.getTemplate( TEMPLATE_ROOM_LIST_ROW );

        while ( rooms.hasMoreElements(  ) )
        {
            HtmlTemplate t = new HtmlTemplate( templateLine );
            ChatRoom room = (ChatRoom) rooms.nextElement(  );
            t.substitute( BOOKMARK_ROOM, room.getName(  ) );
            t.substitute( BOOKMARK_ROOM_DESCRIPTION, room.getDescription(  ) );

            // Select the first room by default
            String strChecked = ( bFirst ) ? "checked=\"checked\"" : "";
            t.substitute( BOOKMARK_CHECKED, strChecked );
            bFirst = false;
            t.substitute( BOOKMARK_GUESTS_COUNT, room.getUserCount(  ) );
            strLines.append( t.getHtml(  ) );
        }

        template.substitute( BOOKMARK_ROWS, strLines.toString(  ) );

        return template.getHtml(  );
    }
}
