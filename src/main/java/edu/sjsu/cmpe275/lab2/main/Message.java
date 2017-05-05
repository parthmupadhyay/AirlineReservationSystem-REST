package edu.sjsu.cmpe275.lab2.main;

import org.json.JSONObject;
import org.json.XML;

/**
 * Created by parth on 4/26/2017.
 */
public class Message
{
    private String message;
    private String code;
    private String messageType;

    /**
     * Constructor
     * @param message Message String
     * @param Code StatusCode for the message
     */
    public Message(String message, String Code) {
        this.message = message;
        this.code = Code;
        setMessageType();
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.setMessageType();
    }

    /**
     * JSON representation of Message
     * @return JSONObject
     */
    public JSONObject getMessageJSON()
    {
        JSONObject error=new JSONObject();
        JSONObject messageType=new JSONObject();
        messageType.put("code",this.code);
        messageType.put("message",this.message);
        error.put(this.messageType,messageType);
        return error;
    }

    /**
     * XML representation of Message
     * @return String
     */
    public String getXML()
    {
        return XML.toString(getMessageJSON());
    }


    /**
     * Set the message type based on the Message statusCode
     * @return Nothing
     */
    private void setMessageType()
    {
        switch (this.code)
        {
            case "200":
            {
                this.messageType="Response";
                break;
            }
            case "404":
            case "400":
            default:
            {
                this.messageType="BadRequest";
            }
        }
    }
}
