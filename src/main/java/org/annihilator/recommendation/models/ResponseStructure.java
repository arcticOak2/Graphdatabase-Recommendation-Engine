package org.annihilator.recommendation.models;

import lombok.Data;

@Data
public class ResponseStructure {

  private int status; // -1 for failure, 0 for data already exist, 1 for success
  private String message;
  private String body;

  public static ResponseStructure getDataExistResponse() {
      ResponseStructure structure = new ResponseStructure();
      structure.setMessage("Data already exist");
      structure.setStatus(0);

      return structure;
  }

  public static ResponseStructure getInvalidInputResponse() {
    ResponseStructure structure = new ResponseStructure();
    structure.setMessage("Invalid Input");
    structure.setStatus(-1);

    return structure;
  }

  public static ResponseStructure getSuccessResponse() {
    ResponseStructure structure = new ResponseStructure();
    structure.setMessage("Success");
    structure.setStatus(1);

    return structure;
  }

  public static ResponseStructure getSuccessResponseWithBody(String message, String body) {
    ResponseStructure structure = new ResponseStructure();
    if(null == message) {
      structure.setMessage("Success");
    } else {
      structure.setMessage(message);
    }
    structure.setStatus(1);
    structure.setBody(body);

    return structure;
  }

  public static ResponseStructure getNotFoundResponse() {
    ResponseStructure structure = new ResponseStructure();
    structure.setMessage("Not Found");
    structure.setStatus(0);

    return structure;
  }
}
