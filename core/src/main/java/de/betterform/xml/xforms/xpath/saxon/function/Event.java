/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.xml.xforms.xpath.saxon.function;

import de.betterform.xml.events.BetterFormEventNames;
import de.betterform.xml.events.XFormsEventNames;
import de.betterform.xml.xforms.Container;
import de.betterform.xml.xforms.XFormsConstants;
import de.betterform.xml.xforms.XFormsElement;
import de.betterform.xml.xforms.action.XFormsAction;
import de.betterform.xml.xforms.ui.Output;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the 7.10.2 The current() Function
 * <p/>
 * Returns the context node used to initialize the evaluation of the containing
 * XPath expression.
 * 
 * @author Nick Van den Bleeken
 * @version $Id$
 */
public class Event extends XFormsFunction {
	/**
     * 
     */
	private static final long serialVersionUID = -4093826587796615212L;

	/**
	 * Pre-evaluate a function at compile time. Functions that do not allow
	 * pre-evaluation, or that need access to context information, can override
	 * this method.
	 * 
	 * @param visitor
	 *            an expression visitor
	 * @return the result of the early evaluation, or the original expression,
	 *         or potentially a simplified expression
	 */

	public Expression preEvaluate(ExpressionVisitor visitor)
			throws XPathException {
		return this;
	}
	
	 /**
     * Iterate over the contents of the collection
     * @return an iterator, whose items will always be nodes (typically but not necessarily document nodes)
     * @throws XPathException
     */

    @SuppressWarnings("unchecked")
	@Override
	public SequenceIterator iterate(final XPathContext xpathContext) throws XPathException {
    	final CharSequence event = argument[0].evaluateAsString(xpathContext);
		return current(xpathContext, event.toString());
    }

	public Sequence call(final XPathContext context,
						 final Sequence[] arguments) throws XPathException {
		final String event = arguments[0].head().toString();
		return SequenceTool.toLazySequence(current(context, event));
	}

	private SequenceIterator current(final XPathContext context, final String event) {
		XPathFunctionContext functionContext = getFunctionContext(context);
		XFormsElement xformsElement = functionContext.getXFormsElement();

		if (!(xformsElement instanceof XFormsAction || xformsElement instanceof Output)) {
			return SingletonIterator.makeIterator(StringValue.EMPTY_STRING);
		}

		Object infoObject = xformsElement.getContainerObject().getCurrentEventInfo();

		if (infoObject instanceof Container.EventInfo) {
			Container.EventInfo eventInfo = (Container.EventInfo) infoObject;

			if("type".equals(event)) {
				return SingletonIterator.makeIterator(new StringValue(eventInfo.getEventType()));
			}
			else if("bubbles".equals(event)) {
				return SingletonIterator.makeIterator(BooleanValue.get(eventInfo.bubbles()));
			}
			else if("cancelable".equals(event)) {
				return SingletonIterator.makeIterator(BooleanValue.get(eventInfo.isCancelable()));
			}
			else if(XFormsEventNames.INSERT.equals(eventInfo.getEventType())) {
				if(XFormsConstants.INSERTED_NODES.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.INSERTED_NODES)) {
					List<Item> insertedNodes = (List) ((Map)eventInfo.getInfo()).get(XFormsConstants.INSERTED_NODES);
					return new ArrayIterator(new ArrayList<Item>(insertedNodes).toArray(new Item[insertedNodes.size()]));
				}
				if(XFormsConstants.ORIGIN_NODES.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.ORIGIN_NODES)) {
					List<Item> originNodes = (List) ((Map)eventInfo.getInfo()).get(XFormsConstants.ORIGIN_NODES);
					return new ArrayIterator(new ArrayList<Item>(originNodes).toArray(new Item[originNodes.size()]));
				}
				if(XFormsConstants.INSERT_LOCATION_NODE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.INSERT_LOCATION_NODE)) {;
					return SingletonIterator.makeIterator((Item)((Map)eventInfo.getInfo()).get(XFormsConstants.INSERT_LOCATION_NODE));
				}
				if(XFormsConstants.POSITION.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.POSITION)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.POSITION))));
				}
			}
			else if(XFormsEventNames.DELETE.equals(eventInfo.getEventType())) {
				if(XFormsConstants.DELETE_LOCATION.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.DELETE_LOCATION)) {
					return SingletonIterator.makeIterator(new DoubleValue(((Double)((Map)eventInfo.getInfo()).get(XFormsConstants.DELETE_LOCATION)).doubleValue()));
				}
				if(XFormsConstants.DELETE_NODES.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.DELETE_NODES)) {
					List<Item> deleteNodes = (List) ((Map)eventInfo.getInfo()).get(XFormsConstants.DELETE_NODES);
					return new ArrayIterator(new ArrayList<Item>(deleteNodes).toArray(new Item[deleteNodes.size()]));
				}
			}
			else if(XFormsEventNames.SUBMIT_ERROR.equals(eventInfo.getEventType())) {
				if(XFormsConstants.ERROR_TYPE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.ERROR_TYPE)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.ERROR_TYPE))));
				}
				if(XFormsConstants.RESOURCE_URI.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESOURCE_URI)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESOURCE_URI))));
				}
				if(XFormsConstants.RESPONSE_STATUS_CODE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_STATUS_CODE)) {
					return SingletonIterator.makeIterator(new DoubleValue(((Double)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_STATUS_CODE)).doubleValue()));
				}
				if(XFormsConstants.RESPONSE_HEADERS.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_HEADERS)) {
					final List<Item> headers = (ArrayList<Item>) ((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_HEADERS);
					Item[] headersAsArray = new Item[headers.size()];
					return new ArrayIterator(headers.toArray(headersAsArray));
				}
				if(XFormsConstants.RESPONSE_REASON_PHRASE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_REASON_PHRASE)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_REASON_PHRASE))));
				}
				if(XFormsConstants.RESPONSE_BODY.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_BODY)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_BODY))));
				}
			}
			else if(XFormsEventNames.SUBMIT_DONE.equals(eventInfo.getEventType())) {
				if(XFormsConstants.RESOURCE_URI.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESOURCE_URI)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESOURCE_URI))));
				}
				if(XFormsConstants.RESPONSE_STATUS_CODE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_STATUS_CODE)) {
					return SingletonIterator.makeIterator(new DoubleValue(((Double)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_STATUS_CODE)).doubleValue()));
				}
				if(XFormsConstants.RESPONSE_HEADERS.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_HEADERS)) {
					final List<Item> headers = (ArrayList<Item>) ((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_HEADERS);
					Item[] headersAsArray = new Item[headers.size()];
					return new ArrayIterator(headers.toArray(headersAsArray));
				}
				if(XFormsConstants.RESPONSE_REASON_PHRASE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_REASON_PHRASE)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_REASON_PHRASE))));
				}
			}
			else if(XFormsEventNames.SUBMIT_SERIALIZE.equals(eventInfo.getEventType())) {
				if(XFormsConstants.SUBMISSION_BODY.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.SUBMISSION_BODY)) {
					return SingletonIterator.makeIterator((Item) ((Map)eventInfo.getInfo()).get(XFormsConstants.SUBMISSION_BODY));
				}
				if(XFormsConstants.RESPONSE_REASON_PHRASE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.RESPONSE_REASON_PHRASE)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.RESPONSE_REASON_PHRASE))));
				}
			}else if(BetterFormEventNames.VARIABLE_CHANGED.equals(eventInfo.getEventType())){
				if(XFormsConstants.VARIABLE_NAME.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.VARIABLE_NAME)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.VARIABLE_NAME))));
				}
				if(XFormsConstants.VARIABLE_VALUE.equals(event) && ((Map)eventInfo.getInfo()).containsKey(XFormsConstants.VARIABLE_VALUE)) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(XFormsConstants.VARIABLE_VALUE))));
				}
			}else{
				if (eventInfo.getInfo() != null) {
					return SingletonIterator.makeIterator(new StringValue(((String)((Map)eventInfo.getInfo()).get(event))));
				}
			}
		}

		return EmptyIterator.getInstance();
	}

	/**
	 * Evaluate in a general context
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Item evaluateItem(XPathContext xpathContext) throws XPathException {
		final CharSequence argAsString = argument[0].evaluateAsString(xpathContext);
		
		XPathFunctionContext functionContext = getFunctionContext(xpathContext);
		XFormsElement xformsElement = functionContext.getXFormsElement();

		Object infoObject = xformsElement.getContainerObject().getCurrentEventInfo();
		
		if (true) {
			throw new RuntimeException("blurp");
		}
		return null;
	}

	//SingletonIterator.makeIterator(
}
