package uw.hcrlab.kubi.view;

import uw.hcrlab.kubi.view.RobotFace.Emotion;
import uw.hcrlab.kubi.view.RobotFace.EyeShape;
import uw.hcrlab.kubi.view.RobotFace.Eye_ID;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;


public class RobotEye {
	// Coordinates of where the eye should be on the screen
	// The origin of the outer shape
	private float x;
	private float y;
	
	// Co-ordinates of the inner eye ball currently
	// These coordinates move frequently
	private float inner_x;
	private float inner_y;
	
	// to draw the eyes
	private Paint paint;
	
	// For oval shape: 
	// the rectangle bound the oval = (outer_radius, 3f/2 * outer_radius)
	// For rectangle shape:
	// height = 2 * outer_radius
	// width = 3 * outer_radius
	private float outer_radius;
	private float inner_radius;
	
	// keeping track of the eye_shape and emotion
	private EyeShape eye_shape;
	private Emotion emotion;
	
	// either left or right eye (for now)
	private Eye_ID side;
	// the degrees in which the eye will rotate for some emotions
	private float alpha;
	
	public RobotEye(float r, Eye_ID side) {
		this(0, 0, r, side);
	}
	
	public RobotEye(float center_x, float center_y, float radius, Eye_ID side){
		this.x = center_x;
		this.y = center_y;
		this.outer_radius = radius;
		this.inner_radius = radius * 2.5f /6 ;
		this.side = side;
		this.eye_shape = EyeShape.CIRCLE;
		this.emotion = Emotion.NORMAL;
		this.alpha = 20;
		if (this.side == Eye_ID.RIGHT) { alpha = - alpha; }
		this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	/* Setters - these do not automatically refresh any drawings on the canvas 
	 * To redraw the eyes, you have to call the drawEye or moveEyeBall function
	 */
	
	public void setCenter(float x, float y) {this.x = x; this.y = y;}
	public void setOuterRadius(float outer) {this.outer_radius = outer;}
	public void setInnerRadius(float inner) {this.inner_radius = inner;}
	
	// these setter automatically redraw the eye
	// but the call to redraw happens in RobotFace
	public void setEyeShape (EyeShape eye_shape) 	{this.eye_shape = eye_shape;}
	public void setEmotion(Emotion state) 			{this.emotion = state;}
		
	/* Getters */
	public float getX() 			{ return this.x; }
	public float getY() 			{ return this.y; }
	public float getOuterRadius() 	{return this.outer_radius; }
	public float getInnerRadius() 	{return this.inner_radius; }
	public EyeShape getEyeShape() 	{return this.eye_shape; }
	public Emotion getEmotion() 	{return this.emotion; }
	
	/* Draw an outer shape with emotion state */
	private void drawOuter(Canvas canvas) {
		switch (this.emotion) {
			case NORMAL: 	drawNormalOuter(canvas);		break;
			case HAPPY:		drawHappyOuter(canvas);			break;
			case IDLE:		drawIdleOuter(canvas);			break;
			case SAD:		drawSadOuter(canvas);			break;
			case SURPRISED: drawSurprisedOuter(canvas);		break;
			case SLEEP:		drawSleepOuter(canvas);			break;
			case GIGGLE:	drawGiggleOuter(canvas);		break;
			case ANGRY:		drawAngryOuter(canvas);			break;
			case WINK:		drawWinkOuter(canvas);			break;
			case ROLLING:	drawRollingOuter(canvas);		break;
			case WORRIED: 	drawWorriedOuter(canvas);		break;
		}
		
	}
	
	private void drawRollingOuter(Canvas canvas) {
		RectF rect;
		if (this.eye_shape == EyeShape.CIRCLE) {
			drawFlexShape(canvas, new PointF(this.x, this.y - 1f/16 * this.outer_radius),
					this.outer_radius, 1f/8 * this.outer_radius,  
					Paint.Style.FILL, Color.WHITE, EyeShape.RECTANGLE);
			rect = createRect(this.x + 1f/2 * this.outer_radius, this.y,
								1f/2 * this.outer_radius, 1f/2 * this.outer_radius);
		} else {
			drawFlexShape(canvas, new PointF(this.x, this.y - 1f/16 * this.outer_radius),
					3f/2 * this.outer_radius, 1f/8 * this.outer_radius,  
					Paint.Style.FILL, Color.WHITE, EyeShape.RECTANGLE);
			rect = createRect(this.x + 3f/4 * this.outer_radius, this.y,
					3f/4 * this.outer_radius, 1f/2 * this.outer_radius);
		}
		if (this.eye_shape == EyeShape.RECTANGLE) {
			rect = createRect(this.x + 3f/4 * this.outer_radius, this.y + 1f/4 * this.outer_radius,
					3f/4 * this.outer_radius, 1f/4 * this.outer_radius);
			canvas.drawRect(rect, paint);
		} else {
			canvas.drawArc(rect, 0, 180, true, paint);
		}
	}

	private void drawWinkOuter(Canvas canvas) {
		if (this.side == Eye_ID.LEFT) {
			drawNormalOuter(canvas);
		} else {
			drawSleepOuter(canvas);
		}
	}

	private void drawSurprisedOuter(Canvas canvas) {
		canvas.save();
		canvas.rotate(this.alpha, this.x, this.y);
		drawNormalOuter(canvas);
		canvas.restore();
	}
	
	private void drawWorriedOuter(Canvas canvas) {
		canvas.save();
		canvas.rotate(-this.alpha, this.x, this.y);
		drawNormalOuter(canvas);
		canvas.restore();
	}

	private void drawAngryOuter(Canvas canvas) {
		canvas.save();
		canvas.rotate(this.alpha, this.x, this.y);
		if (this.eye_shape == EyeShape.CIRCLE) {
			drawFlexShape(canvas, new PointF(this.x, this.y),
					this.outer_radius, 1f/4 * this.outer_radius,  
					Paint.Style.FILL, Color.WHITE, EyeShape.OVAL);
		} else {
			drawFlexShape(canvas, new PointF(this.x, this.y),
					3f/2 * this.outer_radius, 1f/4 * this.outer_radius,  
					Paint.Style.FILL, Color.WHITE, this.eye_shape);
		}
		canvas.restore();
	}

	private void drawGiggleOuter(Canvas canvas) {
		canvas.save();
		canvas.rotate(this.alpha, this.x, this.y);
		drawHappyOuter(canvas);
		canvas.restore();
	}

	private void drawHappyOuter(Canvas canvas) {
		if (this.eye_shape == EyeShape.RECTANGLE) {
			drawFlexShape(canvas, new PointF(this.x, this.y - 2f/3 * this.outer_radius),
					3f/2 * this.outer_radius, 1f/3 * this.outer_radius,  
					Paint.Style.FILL, Color.WHITE, EyeShape.RECTANGLE);
			drawShape(canvas, new PointF(this.x, this.y + 1f/4 * this.outer_radius), 
					this.outer_radius, Paint.Style.FILL, RobotFace.BACKGROUND_COLOR,
					EyeShape.OVAL);
			
		} else {
			drawNormalOuter(canvas);
			drawShape(canvas, new PointF(this.x, this.y + 1f/3 * this.outer_radius), 
				this.outer_radius, Paint.Style.FILL, RobotFace.BACKGROUND_COLOR,
				this.eye_shape);
		}
	}

	private void drawNormalOuter(Canvas canvas) {
		drawShape(canvas, new PointF(this.x, this.y), this.outer_radius, 
				Paint.Style.FILL, Color.WHITE, this.eye_shape);
	}

	private void drawIdleOuter(Canvas canvas) {
		if (this.eye_shape == EyeShape.RECTANGLE) {
			drawFlexShape(canvas, new PointF(this.x, this.y + 2f/3 * this.outer_radius),
					3f/2 * this.outer_radius, 1f/3 * this.outer_radius, 
					Paint.Style.FILL, Color.WHITE, EyeShape.RECTANGLE);
			drawShape(canvas, new PointF(this.x, this.y - 1f/4 * this.outer_radius), 
					this.outer_radius, Paint.Style.FILL, RobotFace.BACKGROUND_COLOR,
					EyeShape.OVAL);
			
		} else {
			drawNormalOuter(canvas);
			drawShape(canvas, new PointF(this.x, this.y - 1f/3 * this.outer_radius), 
				this.outer_radius, Paint.Style.FILL, RobotFace.BACKGROUND_COLOR,
				this.eye_shape);
		}
		
	}

	private void drawSleepOuter(Canvas canvas) {
		drawShape(canvas, new PointF(this.x, this.y), this.outer_radius, 
				Paint.Style.STROKE, Color.WHITE, this.eye_shape);
		drawFlexShape(canvas, new PointF(this.x, this.y - 1f/3 * this.outer_radius), 
				(3f/2 * this.outer_radius) + 2, (2f/3 * this.outer_radius) + 2,  
				Paint.Style.FILL, RobotFace.BACKGROUND_COLOR, EyeShape.RECTANGLE);
		
	}

	private void drawSadOuter (Canvas canvas) {
		if (this.side == Eye_ID.LEFT) {
			drawOneSideSadOuter(canvas, -1);
		} else
			drawOneSideSadOuter(canvas, 1);
	}
	
	private void drawBottomHalf(Canvas canvas) {
		drawNormalOuter(canvas);
		drawFlexShape(canvas, new PointF(this.x, this.y - 1f/2 * this.outer_radius), 
				 (3f/2 * this.outer_radius) + 2, (1f/2 * this.outer_radius) + 2,
				Paint.Style.FILL, RobotFace.BACKGROUND_COLOR, EyeShape.RECTANGLE);
	}

	private void drawOneSideSadOuter(Canvas canvas, float side) {
		drawBottomHalf(canvas);
		float a = 3f/2 * this.outer_radius;
		float b = 1f/4 * this.outer_radius;
		if (this.eye_shape == EyeShape.CIRCLE) {
			a = this.outer_radius;
		} 
		// draw top middle
		drawFlexShape(canvas, new PointF(this.x, this.y), a, b,
				Paint.Style.FILL, RobotFace.BACKGROUND_COLOR, EyeShape.OVAL);
		// erase top side
		drawFlexShape(canvas, new PointF(this.x + 1f/2 * a * side, this.y + b), 1f/2 * a, b, 
				Paint.Style.FILL, RobotFace.BACKGROUND_COLOR, EyeShape.RECTANGLE);
		// draw top side
		RectF rect = createRect(this.x, this.y + 2 * b, a, b);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawArc(rect, 225 + (side * 45), 90, true, paint);
		// draw bottom side
		rect = createRect(this.x, this.y + 2 * b, a, 2 * b);
		canvas.drawArc(rect, 45 + ((-side) * 45), 90, true, paint);
		
	}

	/* 
	 * draw a shape with 
	 * @param canvas
	 * @param p - the origin of the shape
	 * @param radius - the radius of the shape
	 * @param style - fill or stroke
	 * @param color - white or RobotFace.BACKGROUND_COLOR
	 * @param shape - the eye_shape
	 */
	private void drawShape(Canvas canvas, PointF p, float radius, 
			Style style, int color, EyeShape shape) {
		drawFlexShape(canvas, p, 3f/2 * radius, radius, style, color, shape);
	}

	private void drawFlexShape(Canvas canvas, PointF p, float a, float b,
			Style style, int color, EyeShape shape) {
		paint.setStyle(style);
		paint.setColor(color);
		if (shape == EyeShape.CIRCLE) 
			canvas.drawCircle(p.x, p.y, b, paint);
		else {
			RectF rect = createRect(p.x, p.y, a, b);
			if (shape == EyeShape.OVAL)
				canvas.drawOval(rect, paint);
			else
				canvas.drawRect(rect, paint);
		}	
		
	}

	private RectF createRect(float x, float y, float a, float b) {
		float left 		= x - a;
		float right 	= x + a;
		float top 		= y - b;
		float bottom 	= y + b;
		return new RectF(left, top, right, bottom);
	}
	
	/* Move the location of where the eye ball looks at based on a focus point
	 * with the co-ordinates (x, y)
	 */
	public void moveEyeBall(Canvas canvas, float x, float y){
		// Draw Outer Shape
		drawOuter(canvas);
					
		// Handle case where point is inside the eye
		// Calculate where the inner circle center should be 
		if(distanceBetweenPoints(this.x,this.y, x, y) <= this.outer_radius){
			inner_x =  x;
			inner_y =  y;
		} else {
			Point eye_location = calculateEyeBallLocation(x, y, this.x, this.y);
			inner_x = eye_location.x;
			inner_y = eye_location.y;
		}
		
		// Draw Inner circle (eyeball)
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawCircle(inner_x, inner_y, this.inner_radius, this.paint);
	}
	
	/**
	 * Draws a single eye-ball on the screen given the co-ordinates of the inner and outer circles
	 * @param canvas
	 * @param x_outer - the x coordinate of the outer shape
	 * @param y_outer - the y coordinate of the outer shape
	 * @param x_inner - the x coordinate of the inner shape
	 * @param y_inner - the y coordinate of the inner shape
	 */
	public void drawEye(Canvas canvas, float x_outer, float y_outer, float x_inner, float y_inner) {
		// Outer Circle
		drawOuter(canvas);
				
		// Inner circle (eyeball)
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawCircle(x_inner, y_inner, this.inner_radius, this.paint);
	}
	
	public void drawEye(Canvas canvas) {
		this.drawEye(canvas, this.x, this.y, this.x, this.y);
	}
	
	/**
	 * Calculates the intersection between a circle and a line
	 * Note that the radius of the circle depends on the inner_radius.
	 * 
	 * @param x the location of the target x coordinate
	 * @param y the location of the target y coordinate
	 * @param p Center of the circle (x coordinate)
	 * @param q Center of the circle (y coordinate)
	 * @return The intersection point 
	 */
	private Point calculateEyeBallLocation(float x, float y, float p, float q){
		// Calculate the line 
		float m = ( q - y ) / ( p - x);
		float c =  y - m * x;
		
		// Calculate two solutions
		float r = this.outer_radius - this.inner_radius;
		
		// Math formulas to get intersection between a circle and a line
		float x1 = (float) (- 1 * m * c + m * q + p + Math.sqrt(Math.pow((m * c - m * q - p), 2) - (m * m + 1) * ( q * q - r * r + p * p - 2 * c * q + c * c)));
		x1 /= (m * m + 1);
		
		float y1 = m * x1 + c;
		
		float x2 = (float) (- 1 * m * c + m * q + p - Math.sqrt(Math.pow((m * c - m * q - p), 2) - (m * m + 1) * ( q * q - r * r + p * p - 2 * c * q + c * c)));
		x2 /= (m * m + 1);
		
		float y2 = m * x2 + c;
	
		// Find the smallest distance from the center point (p, q)
		float dist1 = distanceBetweenPoints(x1, y1, x, y);
		float dist2 = distanceBetweenPoints(x2, y2, x, y);
		
		return dist1 < dist2 ? new Point((int)x1, (int)y1) : new Point((int)x2, (int)y2); 
	}
	
	private float distanceBetweenPoints(float x1, float y1, float x2, float y2 ){
		return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public void moveLowerLids(Canvas canvas, int i) {
		// the horizontal radius
		float a = this.outer_radius;
		// the vertical radius
		float b = a;
		if (this.eye_shape == EyeShape.OVAL) {
			a = 3f/2 * a;
		}
		
		paint.setColor(Color.WHITE);
		// draw the top bound
		RectF rect = createRect(this.x, this.y, a, b);
		canvas.drawArc(rect, 180, 180, true, paint);
		// draw the bottom bound
		rect = createRect(this.x, this.y, a, Math.abs(b - i));
		if (b > i) {
			canvas.drawArc(rect, 0, 180, true, paint);
		} else {
			paint.setColor(RobotFace.BACKGROUND_COLOR);
			canvas.drawArc(rect, 180, 180, true, paint);
		}	
		// draw the pupil too
		drawPupil(canvas);
	}

	public void moveUpperLids(Canvas canvas, int i) {
		// the horizontal radius
		float a = this.outer_radius;
		// the vertical radius
		float b = a;
		if (this.eye_shape == EyeShape.OVAL) {
			a = 3f/2 * a;
		}

		paint.setColor(Color.WHITE);
		// draw the bottom bound
		RectF rect = createRect(this.x, this.y, a, b);
		canvas.drawArc(rect, 0, 180, true, paint);
		// draw the top bound
		rect = createRect(this.x, this.y, a, Math.abs(b - i));
		if (b > i) {
			canvas.drawArc(rect, 180, 180, true, paint);
		} else {
			paint.setColor(RobotFace.BACKGROUND_COLOR);
			canvas.drawArc(rect, 0, 180, true, paint);
		}
		// draw the pupil too
		drawPupil(canvas);
	}

	public void moveBothLids(Canvas canvas, int i) {
		// the horizontal radius
		float a = this.outer_radius;
		// the vertical radius
		float b = a;
		if (this.eye_shape == EyeShape.OVAL) {
			a = 3f/2 * a;
		}

		canvas.save();
		canvas.rotate(this.alpha, this.x, this.y);
		paint.setColor(Color.WHITE);
		RectF rect = createRect(this.x, this.y, a, b - i);
		canvas.drawOval(rect, paint);
		// draw the pupil too
		drawPupil(canvas);
		canvas.restore();
	}
	
	public void drawPupil(Canvas canvas) {
		paint.setColor(RobotFace.BACKGROUND_COLOR);
		canvas.drawCircle(this.x, this.y, this.inner_radius, paint);
	}
	
	public void drawPupil2(Canvas canvas, float a, float b) {
		paint.setColor(RobotFace.BACKGROUND_COLOR);
		canvas.drawCircle(this.x + a, this.y + b, this.inner_radius, paint);
	}
	
	public void expandEye(Canvas canvas, float i) {
		this.inner_radius += i;
		this.outer_radius += i/2;
		this.drawEye(canvas);
		this.inner_radius -= i;
		this.outer_radius -= i/2;
	}

	public void MoveEyeVertical(Canvas canvas, float i) {
		float oldy = this.y;
		this.y += i;
		this.drawEye(canvas);
		this.y = oldy;
	}
	
	public void changeAngle (Canvas canvas, float alpha) {
		canvas.save();
		canvas.rotate(alpha, this.x, this.y);
		this.drawEye(canvas);
		canvas.restore();
	}
	
	// move the pupil i to the right and j down
	public void movePupil (Canvas canvas, float i, float j) {
		this.drawEye(canvas, this.x, this.y, this.x + i, this.y + j);
	}
	
	// move the pupil x to the right and y down
	public void moveUpperLidsAndPupil (Canvas canvas, float i, float x, float y) {
		// the horizontal radius
		float a = this.outer_radius;
		// the vertical radius
		float b = a;
		if (this.eye_shape == EyeShape.OVAL) {
			a = 3f/2 * a;
		}

		paint.setColor(Color.WHITE);
		// draw the bottom bound
		RectF rect = createRect(this.x, this.y, a, b);
		canvas.drawArc(rect, 0, 180, true, paint);
		// draw the top bound
		rect = createRect(this.x, this.y, a, Math.abs(b - i));
		if (b > i) {
			canvas.drawArc(rect, 180, 180, true, paint);
		} else {
			paint.setColor(RobotFace.BACKGROUND_COLOR);
			canvas.drawArc(rect, 0, 180, true, paint);
		}
		// draw the pupil too
		drawPupil2(canvas, x, y);
		
	}
	
}