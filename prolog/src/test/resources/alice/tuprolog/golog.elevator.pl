% http://www.cs.toronto.edu/cogrobo/kia/simpleElevator

                   %  THE ELEVATOR CONTROLLER

% Primitive control actions

primitive_action(turnoff(N)).  % Turn off call button N.
primitive_action(open).        % Open elevator door.
primitive_action(close).       % Close elevator door.
primitive_action(up(N)).       % Move elevator up to floor N.
primitive_action(down(N)).     % Move elevator down to floor N.

% Definitions of Complex Control Actions

proc(goFloor(N), ?(currentFloor(N)) # up(N) # down(N)).
proc(serve(N), goFloor(N) : turnoff(N) : open : close).
proc(serveAfloor, pi(n, ?(nextFloor(n)) : serve(n))).
proc(park, if(currentFloor(0), open, down(0) : open)).

/* control is the main loop. So long as there is an active call
   button, it serves one floor. When all buttons are off, it
   parks the elevator.   */

proc(control, while(some(n, on(n)), serveAfloor) : park).

% Preconditions for Primitive Actions.

poss(up(N),S) :- currentFloor(M,S), M < N.
poss(down(N),S) :- currentFloor(M,S), M > N.
poss(open,S).
poss(close,S).
poss(turnoff(N),S) :- on(N,S).

% Successor State Axioms for Primitive Fluents.

currentFloor(M,do(A,S)) :- A = up(M) ; A = down(M) ;
              '\+'(A) = up(N), '\+'(A) = down(N), currentFloor(M,S).

on(M,do(A,S)) :- on(M,S), '\+'(A) = turnoff(M).


/* nextFloor(N,S) is an abbreviation that determines which of the
   active call buttons should be served next. Here, we simply
   choose an arbitrary active call button.   */

nextFloor(N,S) :- on(N,S).

% Restore suppressed situation arguments.

restoreSitArg(on(N),S,on(N,S)).
restoreSitArg(nextFloor(N),S,nextFloor(N,S)).
restoreSitArg(currentFloor(M),S,currentFloor(M,S)).


% Initial Situation. Call buttons: 3 and 5. The elevator is at floor 4.
on(3,s0).   on(5,s0).   currentFloor(4,s0).
