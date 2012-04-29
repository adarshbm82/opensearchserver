/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AdvancedController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2000726412587800459L;

	public AdvancedController() throws SearchLibException {
		super();
	}

	public PropertyItem<Integer> getMaxClauseCount() {
		return ClientFactory.INSTANCE.getBooleanQueryMaxClauseCount();
	}

	@Override
	protected void reset() throws SearchLibException {
		// TODO Auto-generated method stub
	}

}
