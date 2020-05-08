import { Directive, ElementRef, Input, AfterContentChecked } from '@angular/core';

@Directive({
  selector: '[focus]'
})
export class FocusDirective implements AfterContentChecked {

  @Input() focus: boolean;
  private hasFocus = false;

  constructor(private elementRef: ElementRef) {
  }

  ngAfterContentChecked() {
    if (this.focus && !this.hasFocus) {
      this.elementRef.nativeElement.focus();
      this.hasFocus = true;
    }
  }
}
