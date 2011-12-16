/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.bank.model.entity.ref;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.excilys.ebi.bank.model.IConstants;

@Entity(name = "REF_CARD_TYPE")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = IConstants.Cache.ENTITY_CACHE)
@Immutable
public class CardTypeRef extends Ref {

	private static final long serialVersionUID = 3416012584227710916L;

	private CardType id;

	@Id
	@Column(name = "ID", length = 20)
	@Enumerated(EnumType.STRING)
	public CardType getId() {
		return id;
	}

	public void setId(CardType id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CardTypeRef other = (CardTypeRef) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
